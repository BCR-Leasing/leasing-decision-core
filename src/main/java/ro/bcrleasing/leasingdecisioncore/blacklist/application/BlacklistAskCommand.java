package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import java.util.UUID;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

public record BlacklistAskCommand(
        UUID requestId,
        long leaseId,
        SubjectType subjectType,
        String subjectId) {
}
