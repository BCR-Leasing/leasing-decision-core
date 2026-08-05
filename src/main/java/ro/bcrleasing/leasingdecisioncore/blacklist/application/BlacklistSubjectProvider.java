package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

public interface BlacklistSubjectProvider {
    SubjectType supportedType();

    BlacklistSubject loadSubject(long leaseId, String subjectId);
}
