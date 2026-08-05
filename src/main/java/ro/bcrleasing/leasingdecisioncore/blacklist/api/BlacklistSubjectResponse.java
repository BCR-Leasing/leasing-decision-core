package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

public record BlacklistSubjectResponse(
        SubjectType type,
        String id,
        String displayName,
        String role) { }
