package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;

final class SubjectIdParser {

    private SubjectIdParser() {
    }

    static long parseLong(String subjectId, SubjectType subjectType) {
        if (subjectId == null || subjectId.isBlank()) {
            throw new InvalidSubjectDataException(
                    "subjectId is required for " + subjectType
            );
        }

        try {
            return Long.parseLong(subjectId);
        } catch (NumberFormatException exception) {
            throw new InvalidSubjectDataException(
                    "subjectId must be numeric for " + subjectType,
                    exception
            );
        }
    }
}
