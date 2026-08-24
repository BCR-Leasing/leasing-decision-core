package ro.bcrleasing.leasingdecisioncore.dowjones.application;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

import java.util.UUID;

public record DowJonesAskCommand(
        UUID requestId,
        long leaseId,
        SubjectType subjectType,
        String subjectId
) {
}
