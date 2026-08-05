package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

public record BlacklistSubject(
        SubjectType type,
        String sourceId,
        String identifier,
        String name,
        String role
) {
}
