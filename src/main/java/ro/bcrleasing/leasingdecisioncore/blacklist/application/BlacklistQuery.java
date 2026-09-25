package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import java.util.Objects;

import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;

public record BlacklistQuery(
        String cnpCui,
        String clientName
) {

    public BlacklistQuery {
        cnpCui = Objects.requireNonNull(
                cnpCui,
                "cnpCui is required"
        ).trim();

        clientName = normalizeOptionalValue(
                clientName
        );

        if (cnpCui.isBlank()) {
            throw new InvalidSubjectDataException(
                    "cnpCUI is required."
            );
        }

        if (!cnpCui.matches("\\d+")) {
            throw new InvalidSubjectDataException(
                    "cnpCUI must contain only digits."
            );
        }
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
