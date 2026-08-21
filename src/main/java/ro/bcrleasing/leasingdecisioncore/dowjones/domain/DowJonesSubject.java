package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.util.Objects;

public record DowJonesSubject(
        DowJonesSubjectType type,
        String sourceId,
        String displayName
) {

    public DowJonesSubject {
        Objects.requireNonNull(
                type,
                "type is required"
        );

        Objects.requireNonNull(
                sourceId,
                "sourceId is required"
        );

        Objects.requireNonNull(
                displayName,
                "displayName is required"
        );
    }
}
