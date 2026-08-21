package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubjectType;

public record DowJonesSubjectResponse(
        DowJonesSubjectType type,
        String id,
        String displayName
) {
}
