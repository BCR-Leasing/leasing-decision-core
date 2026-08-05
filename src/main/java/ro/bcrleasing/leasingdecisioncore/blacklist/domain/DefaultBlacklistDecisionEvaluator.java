package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class DefaultBlacklistDecisionEvaluator implements BlacklistDecisionEvaluator {

    public static final String RULE_VERSION = "blacklist-legacy-v1";
    private static final String NORKOM_NOT_FOUND_TEXT = "The name was NOT found";

    @Override
    public BlacklistDecision evaluate(PreparedBlacklistInput input) {
        List<ReasonCode> reasons = new ArrayList<>();
        List<DecisionFinding> findings = new ArrayList<>();

        if (isBlank(input.subject().identifier())) {
            reasons.add(ReasonCode.IDENTIFIER_MISSING);
            findings.add(new DecisionFinding(
                    FindingSource.VALIDATION,
                    FindingCategory.IDENTIFIER,
                    Map.of()
            ));
        }

        for (SibcorBlacklistItem item : input.sibcorFacts().blacklistItems()) {
            if ("true".equals(item.foundFlag())) {
                reasons.add(ReasonCode.SIBCOR_BLACKLIST_MATCH);
                findings.add(new DecisionFinding(
                        FindingSource.SIBCOR,
                        FindingCategory.BLACKLIST,
                        item.details()
                ));
            }
        }

        SibcorRiskData risk = input.sibcorFacts().risk();
        if (risk != null
                && risk.foundFlag() != null
                && !"N".equals(risk.foundFlag())) {
            reasons.add(ReasonCode.SIBCOR_RISK_MATCH);
            findings.add(new DecisionFinding(
                    FindingSource.SIBCOR,
                    FindingCategory.RISK,
                    risk.details()
            ));
        }

        SibcorNorkomData norkom = input.sibcorFacts().norkom();
        if (norkom != null && isNegativeNorkom(norkom)) {
            reasons.add(ReasonCode.SIBCOR_NORKOM_MATCH);
            findings.add(new DecisionFinding(
                    FindingSource.SIBCOR,
                    FindingCategory.NORKOM,
                    norkom.details()
            ));
        }

        if (input.internalFacts().matched()) {
            reasons.add(ReasonCode.INTERNAL_NEGATIVE_INFORMATION_MATCH);
            findings.add(new DecisionFinding(
                    FindingSource.BCRL_INTERNAL_NEGATIVE_INFORMATION,
                    FindingCategory.INTERNAL_LIST,
                    input.internalFacts().details()
            ));
        }

        List<ReasonCode> uniqueReasons =
                List.copyOf(new LinkedHashSet<>(reasons));

        Verdict verdict = uniqueReasons.isEmpty()
                ? Verdict.PASSED
                : Verdict.FAILED;

        return new BlacklistDecision(
                input.subject(),
                verdict,
                uniqueReasons,
                findings,
                RULE_VERSION
        );
    }

    private boolean isNegativeNorkom(SibcorNorkomData norkom) {
        return norkom.score() != null
                || (norkom.text() != null
                    && !NORKOM_NOT_FOUND_TEXT.equals(norkom.text()))
                || norkom.userAction() != null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
