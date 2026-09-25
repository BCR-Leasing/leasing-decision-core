package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Objects;

public record BlacklistBatchItem(
        String cnpCui,
        String clientName,
        BlacklistItemStatus status,
        BlacklistDecision decision,
        String errorCode,
        String errorMessage
) {

    public BlacklistBatchItem {
        cnpCui = Objects.requireNonNull(
                cnpCui,
                "cnpCui is required"
        ).trim();

        clientName = normalizeOptionalValue(
                clientName
        );

        status = Objects.requireNonNull(
                status,
                "status is required"
        );

        if (cnpCui.isBlank()) {
            throw new IllegalArgumentException(
                    "cnpCui cannot be blank"
            );
        }

        if (status == BlacklistItemStatus.COMPLETED) {
            Objects.requireNonNull(
                    decision,
                    "decision is required when status is COMPLETED"
            );

            if (clientName == null) {
                throw new IllegalArgumentException(
                        "clientName is required when status is COMPLETED"
                );
            }

            if (errorCode != null || errorMessage != null) {
                throw new IllegalArgumentException(
                        "error fields must be null when status is COMPLETED"
                );
            }
        }

        if (status == BlacklistItemStatus.NOT_FOUND) {
            if (decision != null) {
                throw new IllegalArgumentException(
                        "decision must be null when status is NOT_FOUND"
                );
            }

            if (errorCode == null || errorCode.isBlank()) {
                throw new IllegalArgumentException(
                        "errorCode is required when status is NOT_FOUND"
                );
            }

            if (errorMessage == null || errorMessage.isBlank()) {
                throw new IllegalArgumentException(
                        "errorMessage is required when status is NOT_FOUND"
                );
            }
        }
    }

    public static BlacklistBatchItem completed(
            String cnpCui,
            String clientName,
            BlacklistDecision decision
    ) {
        return new BlacklistBatchItem(
                cnpCui,
                clientName,
                BlacklistItemStatus.COMPLETED,
                Objects.requireNonNull(
                        decision,
                        "decision is required"
                ),
                null,
                null
        );
    }

    public static BlacklistBatchItem clientNameNotResolved(
            String cnpCui
    ) {
        return new BlacklistBatchItem(
                cnpCui,
                null,
                BlacklistItemStatus.NOT_FOUND,
                null,
                "CLIENT_NAME_NOT_RESOLVED",
                "clientName was not provided and no company name "
                        + "could be resolved for the supplied cnpCUI."
        );
    }

    private static String normalizeOptionalValue(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
