package ro.bcrleasing.leasingdecisioncore.dowjones.application;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubjectType;

import java.util.UUID;

public record DowJonesAskCommand(
        UUID requestId,
        long leaseId,
        DowJonesSubjectType subjectType,
        String subjectId
) {
}
