package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.List;

public record BlacklistDecision(
        BlacklistSubject subject,
        Verdict verdict,
        List<ReasonCode> reasonCodes,
        List<DecisionFinding> findings,
        String ruleVersion
) {
    public BlacklistDecision {
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        findings = findings == null ? List.of() : List.copyOf(findings);
    }
}
