package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Objects;
import java.util.Optional;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;

public record PreparedBlacklistInput(
        BlacklistSubject subject,
        SibcorFacts sibcorFacts,
        InternalNegativeInformationFacts internalFacts,
        Optional<DowJonesScreeningFacts>
        dowJonesScreeningFacts
) {

    public PreparedBlacklistInput {
        subject =
                Objects.requireNonNull(
                        subject,
                        "subject is required"
                );

        sibcorFacts =
                sibcorFacts == null
                        ? SibcorFacts.empty()
                        : sibcorFacts;

        internalFacts =
                internalFacts == null
                        ? InternalNegativeInformationFacts
                        .notChecked()
                        : internalFacts;

        dowJonesScreeningFacts =
                dowJonesScreeningFacts == null
                        ? Optional.empty()
                        : dowJonesScreeningFacts;
    }
}