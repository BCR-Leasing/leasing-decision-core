package ro.bcrleasing.leasingdecisioncore.ask.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ro.bcrleasing.leasingdecisioncore.blacklist.application.BlacklistAskCommand;
import ro.bcrleasing.leasingdecisioncore.blacklist.application.CheckBlacklistUseCase;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecision;

@Service
public class AskOrchestrator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AskOrchestrator.class);
    private final DecisionContextFactory contextFactory;
    private final CheckBlacklistUseCase checkBlacklistUseCase;
    private final Clock clock;

    public AskOrchestrator(DecisionContextFactory contextFactory, CheckBlacklistUseCase checkBlacklistUseCase) {
        this(contextFactory, checkBlacklistUseCase, Clock.systemUTC());
    }

    AskOrchestrator(DecisionContextFactory contextFactory, CheckBlacklistUseCase checkBlacklistUseCase, Clock clock) {
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory is required");
        this.checkBlacklistUseCase = Objects.requireNonNull(checkBlacklistUseCase, "checkBlacklistUseCase is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    public BlacklistDecisionEnvelope executeBlacklist(BlacklistAskCommand command) {
        Objects.requireNonNull(command, "command is required");
        DecisionContext context = contextFactory.create(command.requestId());
        BlacklistDecision decision = checkBlacklistUseCase.check(command);
        Instant completedAt = Instant.now(clock);
        long durationMs = Duration.between(context.startedAt(), completedAt).toMillis();
        LOGGER.info("Blacklist ask completed. " + "requestId={}, askId={}, subjectType={}, " + "verdict={}, durationMs={}", context.requestId(), context.askId(), command.subjectType(), decision.verdict(), durationMs);
        return new BlacklistDecisionEnvelope(context, decision, completedAt);
    }
}