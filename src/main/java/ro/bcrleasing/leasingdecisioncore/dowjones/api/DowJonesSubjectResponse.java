package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

public record DowJonesSubjectResponse(
        SubjectType type,
        String id,
        String displayName
) {
}
