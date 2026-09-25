package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.ProcessingStatus;

public record BlacklistDecisionResponse(
        UUID requestId,
        UUID sessionId,
        UUID askId,
        ProcessingStatus status,
        List<BlacklistDecisionItemResponse> results,
        Instant completedAt
) {

    public BlacklistDecisionResponse {
        results =
                results == null
                        ? List.of()
                        : List.copyOf(
                        results
                );
    }
}