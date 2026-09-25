package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.List;
import java.util.Objects;

public record BlacklistBatchDecision(
        List<BlacklistBatchItem> items
) {

    public BlacklistBatchDecision {
        Objects.requireNonNull(
                items,
                "items are required"
        );

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one blacklist item is required."
            );
        }

        items = List.copyOf(items);
    }
}
