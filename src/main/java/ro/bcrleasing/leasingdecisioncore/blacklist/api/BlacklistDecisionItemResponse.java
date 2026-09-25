package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.util.List;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistItemStatus;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.KoResult;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.ReasonCode;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.Verdict;

public record BlacklistDecisionItemResponse(
        String cnpCui,
        String clientName,
        BlacklistItemStatus status,
        Verdict verdict,
        KoResult koResult,
        BlacklistSubjectResponse subject,
        List<ReasonCode> reasonCodes,
        List<DecisionFindingResponse> findings,
        String errorCode,
        String errorMessage
) {

    public BlacklistDecisionItemResponse {
        reasonCodes = reasonCodes == null
                ? List.of()
                : List.copyOf(reasonCodes);

        findings = findings == null
                ? List.of()
                : List.copyOf(findings);
    }
}
