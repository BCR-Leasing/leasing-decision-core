package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

import java.util.Arrays;
import java.util.Objects;

public record GeneratedDowJonesDocument(
        byte[] content,
        String contentType,
        String fileName
) {

    public GeneratedDowJonesDocument {
        Objects.requireNonNull(
                content,
                "content is required"
        );

        Objects.requireNonNull(
                contentType,
                "contentType is required"
        );

        Objects.requireNonNull(
                fileName,
                "fileName is required"
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
