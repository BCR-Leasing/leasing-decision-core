package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesMatch;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningStatus;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubject;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.GeneratedDowJonesDocument;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesScreeningPort;

@Component
public class PlaywrightDowJonesAdapter implements DowJonesScreeningPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightDowJonesAdapter.class);
    private static final String CAPABILITY = "DOW_JONES";

    private final DowJonesProperties properties;
    private final DowJonesPdfExtractor pdfExtractor;
    private final DowJonesFileStorage fileStorage;
    private final Semaphore semaphore;

    public PlaywrightDowJonesAdapter(
            DowJonesProperties properties,
            DowJonesPdfExtractor pdfExtractor,
            DowJonesFileStorage fileStorage
    ) {
        this.properties = Objects.requireNonNull(properties, "properties is required");
        this.pdfExtractor = Objects.requireNonNull(pdfExtractor, "pdfExtractor is required");
        this.fileStorage = Objects.requireNonNull(fileStorage, "fileStorage is required");
        this.semaphore = new Semaphore(Math.max(1, properties.getMaxConcurrentScreenings()), true);
    }

    @Override
    public DowJonesScreeningFacts screen(DowJonesSubject subject) {
        Objects.requireNonNull(subject, "subject is required");
        validateConfiguration();
        boolean acquired = false;

        try {
            acquired = semaphore.tryAcquire(
                    properties.getSearchTimeout().toMillis(),
                    TimeUnit.MILLISECONDS
            );

            if (!acquired) {
                throw new ExternalCapabilityException(
                        CAPABILITY,
                        "No Dow Jones browser slot became available in time."
                );
            }

            return executeScreening(subject);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones screening was interrupted.",
                    exception
            );
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

    private DowJonesScreeningFacts executeScreening(DowJonesSubject subject) {
        String configuredBrowserExecutablePath = properties.getBrowserExecutablePath();
        if (!StringUtils.hasText(configuredBrowserExecutablePath)) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones Chromium executable path is not configured."
            );
        }

        final Path browserExecutablePath;
        try {
            browserExecutablePath = Path.of(configuredBrowserExecutablePath.trim())
                    .toAbsolutePath()
                    .normalize();
        } catch (RuntimeException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones Chromium executable path is invalid.",
                    exception
            );
        }

        if (!Files.isRegularFile(browserExecutablePath)) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones Chromium executable does not exist: " + browserExecutablePath
            );
        }

        Map<String, String> playwrightEnvironment = new HashMap<>(System.getenv());
        playwrightEnvironment.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
        Playwright.CreateOptions createOptions = new Playwright.CreateOptions()
                .setEnv(playwrightEnvironment);

        LOGGER.info(
                "Starting Dow Jones browser screening. subjectType={}, browserExecutable={}, headless={}",
                subject.type(), browserExecutablePath, properties.isHeadless()
        );

        try (Playwright playwright = Playwright.create(createOptions)) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(properties.isHeadless())
                    .setSlowMo(properties.getSlowMoMs())
                    .setExecutablePath(browserExecutablePath);

            Browser browser = playwright.chromium().launch(launchOptions);
            try {
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                        .setIgnoreHTTPSErrors(properties.isIgnoreHttpsErrors());
                BrowserContext context = browser.newContext(contextOptions);

                try {
                    Page page = context.newPage();
                    page.setDefaultNavigationTimeout(properties.getNavigationTimeout().toMillis());
                    page.setDefaultTimeout(properties.getSearchTimeout().toMillis());

                    DowJonesPage dowJonesPage = new DowJonesPage(page, properties);
                    dowJonesPage.loginAndOpenSimpleSearch();

                    int resultsFound = dowJonesPage.search(subject.displayName());
                    List<DowJonesMatch> matches = dowJonesPage.readAllMatches(resultsFound);

                    DowJonesScreeningFacts facts = buildScreeningFacts(
                            page, subject, resultsFound, matches
                    );

                    LOGGER.info(
                            "Dow Jones browser screening completed. subjectType={}, status={}, "
                                    + "resultsFound={}, documentGenerated={}",
                            subject.type(), facts.screeningStatus(), resultsFound, facts.document() != null
                    );

                    return facts;
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        } catch (ExternalCapabilityException exception) {
            throw exception;
        } catch (PlaywrightException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones browser automation failed.",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones screening could not be completed.",
                    exception
            );
        }
    }

    DowJonesScreeningFacts buildScreeningFacts(
            Page page,
            DowJonesSubject subject,
            int resultsFound,
            List<DowJonesMatch> matches
    ) {
        if (resultsFound < 0) {
            throw new IllegalArgumentException("resultsFound cannot be negative");
        }

        if (resultsFound == 0) {
            return new DowJonesScreeningFacts(
                    DowJonesScreeningStatus.NO_MATCH,
                    0,
                    matches,
                    null,
                    null
            );
        }

        GeneratedDowJonesDocument document = pdfExtractor.download(page, subject);
        String documentPath = fileStorage.save(document);

        return new DowJonesScreeningFacts(
                DowJonesScreeningStatus.MATCH_FOUND,
                resultsFound,
                matches,
                document,
                documentPath
        );
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getUsername())) {
            throw new ExternalCapabilityException(CAPABILITY, "DOW_JONES_USERNAME is not configured.");
        }
        if (!StringUtils.hasText(properties.getPassword())) {
            throw new ExternalCapabilityException(CAPABILITY, "DOW_JONES_PASSWORD is not configured.");
        }
        if (!StringUtils.hasText(properties.getUrl())) {
            throw new ExternalCapabilityException(CAPABILITY, "DOW_JONES_URL is not configured.");
        }
    }
}
