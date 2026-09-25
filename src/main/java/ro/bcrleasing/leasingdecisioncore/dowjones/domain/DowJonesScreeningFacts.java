package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.util.List;
import java.util.Objects;

public record DowJonesScreeningFacts(
        DowJonesScreeningStatus screeningStatus,
        int resultsFound,
        List<DowJonesMatch> matches,
        GeneratedDowJonesDocument document,
        String documentPath
) {

    public DowJonesScreeningFacts {
        Objects.requireNonNull(screeningStatus, "screeningStatus is required");

        if (resultsFound < 0) {
            throw new IllegalArgumentException("resultsFound cannot be negative");
        }

        matches = matches == null ? List.of() : List.copyOf(matches);
        documentPath = documentPath == null || documentPath.isBlank()
                ? null
                : documentPath;

        if (resultsFound == 0) {
            if (screeningStatus != DowJonesScreeningStatus.NO_MATCH || !matches.isEmpty()) {
                throw new IllegalArgumentException("Zero results require NO_MATCH and an empty matches list");
            }
            if (document != null || documentPath != null) {
                throw new IllegalArgumentException("Zero results must not have a PDF or documentPath");
            }
        } else {
            if (screeningStatus != DowJonesScreeningStatus.MATCH_FOUND) {
                throw new IllegalArgumentException("Positive results require MATCH_FOUND");
            }
            Objects.requireNonNull(document, "document is required for positive results");
            Objects.requireNonNull(documentPath, "documentPath is required for positive results");
        }
    }
}
