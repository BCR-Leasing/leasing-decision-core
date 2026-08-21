package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import java.time.Instant;

public record DowJonesDocumentResponse(
        boolean available,
        String fileName,
        String contentType,
        long size,
        String sha256,
        String downloadUrl,
        Instant expiresAt
) {
}
