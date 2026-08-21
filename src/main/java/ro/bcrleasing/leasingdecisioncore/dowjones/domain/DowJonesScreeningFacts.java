package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.util.List;
import java.util.Objects;

public record DowJonesScreeningFacts(
        DowJonesScreeningStatus screeningStatus,
        int resultsFound,
        List<DowJonesMatch> matches,
        GeneratedDowJonesDocument document
) {

    public DowJonesScreeningFacts {
        Objects.requireNonNull(
                screeningStatus,
                "screeningStatus is required"
        );

        Objects.requireNonNull(
                document,
                "document is required"
        );

        if (resultsFound < 0) {
            throw new IllegalArgumentException(
                    "resultsFound cannot be negative"
            );
        }

        matches = matches == null
                ? List.of()
                : List.copyOf(matches);
    }
}
