package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.ProcessingStatus;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.ReasonCode;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.Verdict;

public record BlacklistDecisionResponse(
        UUID requestId,
        UUID sessionId,
        UUID askId,
        ProcessingStatus status,
        Verdict verdict,
        BlacklistSubjectResponse subject,
        List<ReasonCode> reasonCodes,
        List<DecisionFindingResponse> findings,
        String ruleVersion,
        Instant completedAt) {

    public BlacklistDecisionResponse {
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        findings = findings == null ? List.of() : List.copyOf(findings);
    }
}
