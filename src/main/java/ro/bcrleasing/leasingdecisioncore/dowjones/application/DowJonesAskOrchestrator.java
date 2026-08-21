package ro.bcrleasing.leasingdecisioncore.dowjones.application;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ro.bcrleasing.leasingdecisioncore.ask.application.DecisionContext;
import ro.bcrleasing.leasingdecisioncore.ask.application.DecisionContextFactory;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesResult;

@Service
public class DowJonesAskOrchestrator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    DowJonesAskOrchestrator.class
            );

    private final DecisionContextFactory contextFactory;
    private final CheckDowJonesUseCase checkDowJonesUseCase;

    public DowJonesAskOrchestrator(
            DecisionContextFactory contextFactory,
            CheckDowJonesUseCase checkDowJonesUseCase
    ) {
        this.contextFactory = contextFactory;
        this.checkDowJonesUseCase =
                checkDowJonesUseCase;
    }

    public DowJonesAskEnvelope execute(
            DowJonesAskCommand command
    ) {
        DecisionContext context =
                contextFactory.create(
                        command.requestId()
                );

        DowJonesResult result =
                checkDowJonesUseCase.check(
                        command,
                        context.askId()
                );

        Instant completedAt = Instant.now();

        long durationMs = Duration.between(
                context.startedAt(),
                completedAt
        ).toMillis();

        LOGGER.info(
                "Dow Jones ask completed. "
                        + "requestId={}, askId={}, "
                        + "subjectType={}, subjectId={}, "
                        + "status={}, resultsFound={}, "
                        + "durationMs={}",
                context.requestId(),
                context.askId(),
                command.subjectType(),
                command.subjectId(),
                result.screeningStatus(),
                result.resultsFound(),
                durationMs
        );

        return new DowJonesAskEnvelope(
                context,
                result,
                completedAt
        );
    }
}