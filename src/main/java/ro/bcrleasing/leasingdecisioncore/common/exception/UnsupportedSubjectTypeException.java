package ro.bcrleasing.leasingdecisioncore.common.exception;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

public class UnsupportedSubjectTypeException extends RuntimeException {

    public UnsupportedSubjectTypeException(SubjectType subjectType) {
        super("Unsupported blacklist subject type: " + subjectType);
    }
}
