package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record DowJonesDocumentContent(
        UUID askId,
        String fileName,
        String contentType,
        byte[] content,
        Instant expiresAt
) {

    public DowJonesDocumentContent {
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
                content,
                "content is required"
        );

        Objects.requireNonNull(
                expiresAt,
                "expiresAt is required"
        );

        content = Arrays.copyOf(
                content,
                content.length
        );
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(
                content,
                content.length
        );
    }
}
