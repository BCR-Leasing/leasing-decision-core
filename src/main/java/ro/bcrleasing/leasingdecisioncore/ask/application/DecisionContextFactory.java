package ro.bcrleasing.leasingdecisioncore.ask.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class DecisionContextFactory {
    private final Clock clock;

    public DecisionContextFactory() {
        this(Clock.systemUTC());
    }

    DecisionContextFactory(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public DecisionContext create(UUID requestId) {
        return new DecisionContext(
                Objects.requireNonNull(requestId, "requestId is required"),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(clock));
    }
}