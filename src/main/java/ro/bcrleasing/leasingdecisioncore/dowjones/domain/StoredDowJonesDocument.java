package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record StoredDowJonesDocument(
        UUID askId,
        String fileName,
        String contentType,
        long size,
        String sha256,
        Instant expiresAt
) {

    public StoredDowJonesDocument {
        Objects.requireNonNull(
                askId,
                "askId is required"
        );

        Objects.requireNonNull(
                fileName,
                "fileName is required"
        );

        Objects.requireNonNull(
                contentType,
                "contentType is required"
        );

        Objects.requireNonNull(
                sha256,
                "sha256 is required"
        );

        Objects.requireNonNull(
                expiresAt,
                "expiresAt is required"
        );
    }
}