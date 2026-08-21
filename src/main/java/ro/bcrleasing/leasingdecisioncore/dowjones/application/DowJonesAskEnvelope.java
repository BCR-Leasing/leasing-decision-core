package ro.bcrleasing.leasingdecisioncore.dowjones.application;

import java.time.Instant;
import java.util.Objects;

import ro.bcrleasing.leasingdecisioncore.ask.application.DecisionContext;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesResult;

public record DowJonesAskEnvelope(
        DecisionContext context,
        DowJonesResult result,
        Instant completedAt
) {

    public DowJonesAskEnvelope {
        Objects.requireNonNull(
                context,
                "context is required"
        );

        Objects.requireNonNull(
                result,
                "result is required"
        );

        Objects.requireNonNull(
                completedAt,
                "completedAt is required"
        );
    }
}
