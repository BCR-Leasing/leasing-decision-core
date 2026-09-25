package ro.bcrleasing.leasingdecisioncore.ask.application;

import java.time.Instant;
import java.util.Objects;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistBatchDecision;

public record BlacklistDecisionEnvelope(DecisionContext context, BlacklistBatchDecision decision, Instant completedAt) {

    public BlacklistDecisionEnvelope {
        context = Objects.requireNonNull(context, "context is required");
        decision = Objects.requireNonNull(decision, "decision is required");
        completedAt = Objects.requireNonNull(completedAt, "completedAt is required");
    }
}