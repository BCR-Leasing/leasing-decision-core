package ro.bcrleasing.leasingdecisioncore.ask.application;

import java.time.Instant;
import java.util.Objects;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecision;

public record BlacklistDecisionEnvelope(DecisionContext context,
                                        BlacklistDecision decision,
                                        Instant completedAt) {
    public BlacklistDecisionEnvelope {
        Objects.requireNonNull(context, "context is required");
        Objects.requireNonNull(decision, "decision is required");
        Objects.requireNonNull(completedAt, "completedAt is required");
    }
}