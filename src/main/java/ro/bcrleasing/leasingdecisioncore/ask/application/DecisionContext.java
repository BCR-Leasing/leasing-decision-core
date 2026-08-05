package ro.bcrleasing.leasingdecisioncore.ask.application;

import java.time.Instant;
import java.util.UUID;

public record DecisionContext(
        UUID requestId,
        UUID sessionId,
        UUID askId,
        Instant startedAt) {
}