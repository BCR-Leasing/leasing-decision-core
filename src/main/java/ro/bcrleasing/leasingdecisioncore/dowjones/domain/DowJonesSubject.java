package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

import java.util.Objects;

public record DowJonesSubject(
        SubjectType type,
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
