package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.nio.file.Path;
import java.util.List;
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
public class PlaywrightDowJonesAdapter
        implements DowJonesScreeningPort {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    PlaywrightDowJonesAdapter.class
            );

    private static final String CAPABILITY =
            "DOW_JONES";

    private final DowJonesProperties properties;
    private final DowJonesPdfExtractor pdfExtractor;
    private final Semaphore semaphore;

    public PlaywrightDowJonesAdapter(
            DowJonesProperties properties,
            DowJonesPdfExtractor pdfExtractor
    ) {
        this.properties = properties;
        this.pdfExtractor = pdfExtractor;

        int concurrency = Math.max(
                1,
                properties.getMaxConcurrentScreenings()
        );

        this.semaphore = new Semaphore(
                concurrency,
                true
        );
    }

    @Override
    public DowJonesScreeningFacts screen(
            DowJonesSubject subject
    ) {
        validateConfiguration();

        boolean acquired = false;

        try {
            acquired = semaphore.tryAcquire(
                    properties
                            .getSearchTimeout()
                            .toMillis(),
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

    private DowJonesScreeningFacts executeScreening(
            DowJonesSubject subject
    ) {
        try (Playwright playwright = Playwright.create()) {

            BrowserType.LaunchOptions launchOptions =
                    new BrowserType.LaunchOptions()
                            .setHeadless(
                                    properties.isHeadless()
                            )
                            .setSlowMo(
                                    properties.getSlowMoMs()
                            );

            if (StringUtils.hasText(
                    properties.getBrowserExecutablePath()
            )) {
                launchOptions.setExecutablePath(
                        Path.of(
                                properties
                                        .getBrowserExecutablePath()
                                        .trim()
                        )
                );
            }

            Browser browser =
                    playwright
                            .chromium()
                            .launch(launchOptions);

            try {
                Browser.NewContextOptions contextOptions =
                        new Browser.NewContextOptions()
                                .setIgnoreHTTPSErrors(
                                        properties
                                                .isIgnoreHttpsErrors()
                                );

                BrowserContext context =
                        browser.newContext(
                                contextOptions
                        );

                try {
                    Page page = context.newPage();

                    page.setDefaultNavigationTimeout(
                            properties
                                    .getNavigationTimeout()
                                    .toMillis()
                    );

                    page.setDefaultTimeout(
                            properties
                                    .getSearchTimeout()
                                    .toMillis()
                    );

                    DowJonesPage dowJonesPage =
                            new DowJonesPage(
                                    page,
                                    properties
                            );

                    dowJonesPage
                            .loginAndOpenSimpleSearch();

                    int resultsFound =
                            dowJonesPage.search(
                                    subject.displayName()
                            );

                    List<DowJonesMatch> matches =
                            dowJonesPage.readAllMatches(
                                    resultsFound
                            );

                    GeneratedDowJonesDocument document =
                            pdfExtractor.download(
                                    page,
                                    subject
                            );

                    DowJonesScreeningStatus status =
                            resultsFound == 0
                                    ? DowJonesScreeningStatus.NO_MATCH
                                    : DowJonesScreeningStatus.MATCH_FOUND;

                    LOGGER.info(
                            "Dow Jones browser screening completed. "
                                    + "subjectType={}, subjectId={}, "
                                    + "status={}, resultsFound={}",
                            subject.type(),
                            subject.sourceId(),
                            status,
                            resultsFound
                    );

                    return new DowJonesScreeningFacts(
                            status,
                            resultsFound,
                            matches,
                            document
                    );

                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }

        } catch (
                ExternalCapabilityException exception
        ) {
            throw exception;
        } catch (PlaywrightException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones browser automation failed.",
                    exception
            );
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(
                properties.getUsername()
        )) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "DOW_JONES_USERNAME is not configured."
            );
        }

        if (!StringUtils.hasText(
                properties.getPassword()
        )) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "DOW_JONES_PASSWORD is not configured."
            );
        }

        if (!StringUtils.hasText(
                properties.getUrl()
        )) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "DOW_JONES_URL is not configured."
            );
        }
    }
}
