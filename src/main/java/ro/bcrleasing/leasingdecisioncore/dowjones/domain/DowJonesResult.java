package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.util.List;
import java.util.Objects;

public record DowJonesResult(
        DowJonesSubject subject,
        DowJonesScreeningStatus screeningStatus,
        int resultsFound,
        List<DowJonesMatch> matches,
        StoredDowJonesDocument document
) {

    public DowJonesResult {
        Objects.requireNonNull(
                subject,
                "subject is required"
        );

        Objects.requireNonNull(
                screeningStatus,
                "screeningStatus is required"
        );

        Objects.requireNonNull(
                document,
                "document is required"
        );

        matches = matches == null
                ? List.of()
                : List.copyOf(matches);
    }
}
