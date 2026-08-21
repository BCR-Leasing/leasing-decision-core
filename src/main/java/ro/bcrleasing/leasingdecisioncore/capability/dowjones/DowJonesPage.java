package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesMatch;

final class DowJonesPage {

    private static final String CAPABILITY = "DOW_JONES";

    private static final String USER_INPUT = "#email";
    private static final String PASSWORD_INPUT = "#password-form-item";
    private static final String SIGN_IN_BUTTON = "#signin-btn";

    private static final String SEARCH_MENU =
            "//a[@data-testid='buttonLink' "
                    + "and .//span[normalize-space()='Search']]";

    private static final String SEARCH_NAME_INPUT =
            "input[name='name']";

    private static final String SEARCH_BUTTON =
            "//button[@type='submit' and normalize-space()='Search']";

    private static final String RESULTS_CONTAINER =
            "#search-results";

    private static final String RESULT_ROWS =
            "//div[@id='search-results']"
                    + "//tbody/tr[not(.//td[contains(@class,'no-data')])]";

    private static final String PROFILE_ID_CELL =
            "td.profile-id";

    private static final String NAME_CELL =
            "td.search-results--name a";

    private static final String GENDER_CELL =
            "td.gender";

    private static final String DATE_OF_BIRTH_CELL =
            "td.dob";

    private static final String COUNTRY_CELL =
            "td.country-name";

    private static final String DETAILS_CELL =
            "td.details";

    private static final String SUBSIDIARY_CELL =
            "td.subsidiary";

    private static final String SCORE_CELL =
            "td.search-results--score";

    private static final String NEXT_PAGE_BUTTON =
            "//button["
                    + "normalize-space()='Next' "
                    + "or @aria-label='Next' "
                    + "or .//*[normalize-space()='Next']"
                    + "]";

    private static final Pattern RESULTS_FOUND_PATTERN =
            Pattern.compile(
                    "Results\\s+Found\\s*:?\\s*(\\d+)",
                    Pattern.CASE_INSENSITIVE
            );

    private final Page page;
    private final DowJonesProperties properties;

    DowJonesPage(
            Page page,
            DowJonesProperties properties
    ) {
        this.page = page;
        this.properties = properties;
    }

    void loginAndOpenSimpleSearch() {
        page.navigate(
                properties.getUrl(),
                new Page.NavigateOptions()
                        .setWaitUntil(
                                WaitUntilState.DOMCONTENTLOADED
                        )
                        .setTimeout(
                                millis(
                                        properties.getNavigationTimeout()
                                )
                        )
        );

        waitVisible(
                USER_INPUT,
                properties.getNavigationTimeout()
        );

        page.locator(USER_INPUT)
                .fill(properties.getUsername());

        page.locator(PASSWORD_INPUT)
                .fill(properties.getPassword());

        page.locator(SIGN_IN_BUTTON)
                .click();

        waitVisible(
                SEARCH_MENU,
                properties.getNavigationTimeout()
        );

        page.locator(SEARCH_MENU)
                .first()
                .click();

        waitVisible(
                SEARCH_NAME_INPUT,
                properties.getNavigationTimeout()
        );
    }

    int search(String subjectName) {
        Locator nameInput =
                page.locator(SEARCH_NAME_INPUT);

        nameInput.fill("");
        nameInput.fill(subjectName);

        page.locator(SEARCH_BUTTON)
                .first()
                .click();

        waitVisible(
                RESULTS_CONTAINER,
                properties.getSearchTimeout()
        );

        return waitForResultsFound();
    }

    List<DowJonesMatch> readAllMatches(
            int expectedResults
    ) {
        if (expectedResults == 0) {
            return List.of();
        }

        List<DowJonesMatch> allMatches =
                new ArrayList<>();

        Set<String> pageSignatures =
                new HashSet<>();

        boolean finished = false;

        for (
                int currentPage = 1;
                currentPage <= properties.getMaxPages();
                currentPage++
        ) {
            waitVisible(
                    RESULTS_CONTAINER,
                    properties.getSearchTimeout()
            );

            List<DowJonesMatch> pageMatches =
                    readCurrentPageMatches();

            if (pageMatches.isEmpty()) {
                throw capabilityError(
                        "Dow Jones reports results, but no result rows were found."
                );
            }

            String signature =
                    buildPageSignature(pageMatches);

            if (!pageSignatures.add(signature)) {
                throw capabilityError(
                        "Dow Jones pagination did not advance."
                );
            }

            allMatches.addAll(pageMatches);

            if (!hasNextPage()) {
                finished = true;
                break;
            }

            if (currentPage == properties.getMaxPages()) {
                throw capabilityError(
                        "Dow Jones pagination exceeded the configured maximum."
                );
            }

            moveToNextPage(signature);
        }

        if (!finished) {
            throw capabilityError(
                    "Dow Jones result pagination did not complete."
            );
        }

        if (allMatches.size() != expectedResults) {
            throw capabilityError(
                    "Dow Jones Results Found is "
                            + expectedResults
                            + ", but "
                            + allMatches.size()
                            + " result rows were captured."
            );
        }

        return List.copyOf(allMatches);
    }

    private List<DowJonesMatch> readCurrentPageMatches() {
        Locator rows = page.locator(RESULT_ROWS);
        int rowCount = rows.count();

        List<DowJonesMatch> result =
                new ArrayList<>(rowCount);

        for (int index = 0; index < rowCount; index++) {
            Locator row = rows.nth(index);

            result.add(
                    new DowJonesMatch(
                            textAt(row, PROFILE_ID_CELL),
                            textAt(row, NAME_CELL),
                            textAt(row, GENDER_CELL),
                            textAt(row, DATE_OF_BIRTH_CELL),
                            textAt(row, COUNTRY_CELL),
                            textAt(row, DETAILS_CELL),
                            textAt(row, SUBSIDIARY_CELL),
                            textAt(row, SCORE_CELL)
                    )
            );
        }

        return result;
    }

    private int waitForResultsFound() {
        long deadline =
                System.nanoTime()
                        + properties
                        .getSearchTimeout()
                        .toNanos();

        while (System.nanoTime() < deadline) {
            try {
                String bodyText = page
                        .locator("body")
                        .innerText();

                Matcher matcher =
                        RESULTS_FOUND_PATTERN
                                .matcher(bodyText);

                if (matcher.find()) {
                    return Integer.parseInt(
                            matcher.group(1)
                    );
                }
            } catch (
                    PlaywrightException
                            | NumberFormatException ignored
            ) {
                // The page may still be rendering.
            }

            page.waitForTimeout(250);
        }

        throw capabilityError(
                "Could not determine Dow Jones Results Found value."
        );
    }

    private boolean hasNextPage() {
        Locator nextButtons =
                page.locator(NEXT_PAGE_BUTTON);

        if (nextButtons.count() == 0) {
            return false;
        }

        Locator next = nextButtons.first();

        if (!next.isVisible()) {
            return false;
        }

        String ariaDisabled =
                next.getAttribute("aria-disabled");

        if ("true".equalsIgnoreCase(ariaDisabled)) {
            return false;
        }

        try {
            return !next.isDisabled();
        } catch (PlaywrightException ignored) {
            return true;
        }
    }

    private void moveToNextPage(
            String previousSignature
    ) {
        Locator next = page
                .locator(NEXT_PAGE_BUTTON)
                .first();

        next.click();

        waitUntil(
                () -> {
                    List<DowJonesMatch> current =
                            readCurrentPageMatches();

                    if (current.isEmpty()) {
                        return false;
                    }

                    return !previousSignature.equals(
                            buildPageSignature(current)
                    );
                },
                properties.getSearchTimeout(),
                "Dow Jones did not load the next page."
        );
    }

    private String buildPageSignature(
            List<DowJonesMatch> matches
    ) {
        return matches.stream()
                .map(match ->
                        match.profileId()
                                + "|"
                                + match.name()
                                + "|"
                                + match.score()
                )
                .reduce(
                        "",
                        (left, right) ->
                                left + "||" + right
                );
    }

    private String textAt(
            Locator row,
            String cellSelector
    ) {
        Locator value =
                row.locator(cellSelector);

        if (value.count() == 0) {
            return "";
        }

        String text = value
                .first()
                .innerText();

        return text == null
                ? ""
                : text.trim();
    }

    private void waitVisible(
            String selector,
            Duration timeout
    ) {
        page.locator(selector)
                .first()
                .waitFor(
                        new Locator.WaitForOptions()
                                .setState(
                                        WaitForSelectorState.VISIBLE
                                )
                                .setTimeout(
                                        millis(timeout)
                                )
                );
    }

    private void waitUntil(
            BooleanSupplier condition,
            Duration timeout,
            String failureMessage
    ) {
        long deadline =
                System.nanoTime()
                        + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (PlaywrightException ignored) {
                // The DOM can change while pagination is advancing.
            }

            page.waitForTimeout(250);
        }

        throw capabilityError(failureMessage);
    }

    private double millis(Duration duration) {
        return duration.toMillis();
    }

    private ExternalCapabilityException capabilityError(
            String message
    ) {
        return new ExternalCapabilityException(
                CAPABILITY,
                message
        );
    }
}
