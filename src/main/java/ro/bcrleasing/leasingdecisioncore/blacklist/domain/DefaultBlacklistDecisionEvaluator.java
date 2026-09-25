package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;

public final class DefaultBlacklistDecisionEvaluator implements BlacklistDecisionEvaluator {

    private static final String COMPLETED = "COMPLETED";
    private static final String OK = "OK";
    private static final String NOK = "NOK";

    @Override
    public BlacklistDecision evaluate(PreparedBlacklistInput input) {
        Objects.requireNonNull(input, "input is required");

        if (isBlank(input.subject().identifier())) {
            return missingIdentifierDecision(input.subject());
        }

        SibcorFacts sibcorFacts = input.sibcorFacts();
        InternalNegativeInformationFacts internalFacts = input.internalFacts();
        boolean sibcorMatch = sibcorFacts.hasBlacklistMatch();
        validateRequiredChecks(input, sibcorMatch, internalFacts);
        boolean dowJonesMatch = input.dowJonesScreeningFacts().map(this::hasDowJonesMatch).orElse(false);
        boolean blacklistProcessNotOk = sibcorMatch && dowJonesMatch;
        boolean restrictedEntitiesProcessNotOk = internalFacts.matched();
        List<ReasonCode> reasonCodes = new ArrayList<>();

        if (blacklistProcessNotOk) {
            reasonCodes.add(ReasonCode.DOW_JONES_MATCH);
        }

        if (restrictedEntitiesProcessNotOk) {
            reasonCodes.add(ReasonCode.RESTRICTED_ENTITY_MATCH);
        }

        List<DecisionFinding> findings = new ArrayList<>();
        findings.add(toSibcorFinding(sibcorFacts, sibcorMatch));
        input.dowJonesScreeningFacts().ifPresent(facts -> findings.add(toDowJonesFinding(facts)));
        findings.add(toRestrictedEntitiesFinding(internalFacts));
        boolean finalNotOk = blacklistProcessNotOk || restrictedEntitiesProcessNotOk;
        Verdict verdict = finalNotOk ? Verdict.FAILED : Verdict.PASSED;

        return new BlacklistDecision(input.subject(), verdict, List.copyOf(reasonCodes), List.copyOf(findings));
    }

    private void validateRequiredChecks(PreparedBlacklistInput input, boolean sibcorMatch, InternalNegativeInformationFacts internalFacts) {

        if (!internalFacts.checked()) {
            throw new IllegalStateException("Restricted entities verification was required but was not executed.");
        }

        boolean dowJonesExecuted = input.dowJonesScreeningFacts().isPresent();

        if (sibcorMatch && !dowJonesExecuted) {
            throw new IllegalStateException("Dow Jones screening was required because SIBCOR returned a blacklist match, "
                    + "but no Dow Jones result was provided.");
        }

        if (!sibcorMatch && dowJonesExecuted) {
            throw new IllegalStateException("Dow Jones screening was executed although SIBCOR did not return a blacklist match.");
        }
    }

    private boolean hasDowJonesMatch(DowJonesScreeningFacts facts) {
        return facts.resultsFound() > 0;
    }

    private DecisionFinding toSibcorFinding(SibcorFacts facts, boolean matched) {
        Map<String, Object> details = new LinkedHashMap<>();

        details.put("processingStatus", COMPLETED);
        details.put("checked", true);
        details.put("reportedBlackListed", facts.blackListed());
        details.put("matched", matched);
        details.put("koResult", toKoResult(matched));
        details.put("dowJonesRequired", matched);
        details.put("response", facts.response());

        return new DecisionFinding(FindingSource.SIBCOR, FindingCategory.BLACKLIST, details);
    }

    private DecisionFinding toDowJonesFinding(DowJonesScreeningFacts facts) {
        boolean matched = hasDowJonesMatch(facts);

        Map<String, Object> details = new LinkedHashMap<>();

        details.put("processingStatus", COMPLETED);
        details.put("checked", true);
        details.put("screeningStatus", facts.screeningStatus().name());
        details.put("resultsFound", facts.resultsFound());
        details.put("matched", matched);
        details.put("koResult", toKoResult(matched));
        details.put("matches", facts.matches());
        details.put("documentGenerated", facts.document() != null);
        details.put("documentPath", facts.documentPath());

        return new DecisionFinding(FindingSource.DOW_JONES, FindingCategory.SCREENING, details);
    }

    private DecisionFinding toRestrictedEntitiesFinding(InternalNegativeInformationFacts facts) {
        Map<String, Object> details = new LinkedHashMap<>();

        details.put("processingStatus", COMPLETED);
        details.put("checked", facts.checked());
        details.put("matched", facts.matched());
        details.put("koResult", toKoResult(facts.matched()));

        if (!facts.details().isEmpty()) {
            details.put("entity", facts.details());
        }

        return new DecisionFinding(FindingSource.RESTRICTED_ENTITIES, FindingCategory.RESTRICTED_ENTITY, details);
    }

    private String toKoResult(boolean negativeResult) {
        return negativeResult ? NOK : OK;
    }

    private BlacklistDecision missingIdentifierDecision(BlacklistSubject subject) {
        DecisionFinding validationFinding =
                new DecisionFinding(
                        FindingSource.VALIDATION,
                        FindingCategory.IDENTIFIER,
                        Map.of("processingStatus", "FAILED", "message", "The subject identifier is missing."));

        return new BlacklistDecision(
                subject, Verdict.FAILED,
                List.of(ReasonCode.IDENTIFIER_MISSING),
                List.of(validationFinding));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
