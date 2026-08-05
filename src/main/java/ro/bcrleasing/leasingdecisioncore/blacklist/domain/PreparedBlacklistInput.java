package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Objects;

public record PreparedBlacklistInput(
        BlacklistSubject subject,
        SibcorFacts sibcorFacts,
        InternalNegativeInformationFacts internalFacts
) {
    public PreparedBlacklistInput {
        Objects.requireNonNull(subject, "subject is required");
        sibcorFacts = sibcorFacts == null ? SibcorFacts.empty() : sibcorFacts;
        internalFacts = internalFacts == null
                ? InternalNegativeInformationFacts.notChecked()
                : internalFacts;
    }
}
