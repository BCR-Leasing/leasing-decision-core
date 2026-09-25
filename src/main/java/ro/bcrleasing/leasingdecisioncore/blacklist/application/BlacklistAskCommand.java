package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;

public record BlacklistAskCommand(UUID requestId, List<BlacklistQuery> queries) {

    public BlacklistAskCommand {
        requestId = Objects.requireNonNull(requestId, "requestId is required");

        if (queries == null || queries.isEmpty()) {
            throw new InvalidSubjectDataException("At least one blacklist query is required.");
        }

        if (queries.stream().anyMatch(Objects::isNull)) {
            throw new InvalidSubjectDataException("Blacklist queries cannot contain null entries.");
        }

        queries = List.copyOf(queries);
    }
}
