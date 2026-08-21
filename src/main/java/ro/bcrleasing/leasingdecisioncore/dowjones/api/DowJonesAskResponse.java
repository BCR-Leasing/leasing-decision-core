package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesProcessingStatus;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningStatus;

public record DowJonesAskResponse(
        UUID requestId,
        UUID sessionId,
        UUID askId,
        DowJonesProcessingStatus status,
        DowJonesScreeningStatus screeningStatus,
        int resultsFound,
        DowJonesSubjectResponse subject,
        List<DowJonesMatchResponse> matches,
        DowJonesDocumentResponse document,
        Instant completedAt
) {
    public DowJonesAskResponse {
        matches = matches == null
                ? List.of()
                : List.copyOf(matches);
    }
}
