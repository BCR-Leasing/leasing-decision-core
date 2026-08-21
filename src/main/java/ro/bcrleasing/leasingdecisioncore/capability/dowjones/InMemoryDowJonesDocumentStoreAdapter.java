package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesDocumentContent;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.GeneratedDowJonesDocument;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.StoredDowJonesDocument;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesDocumentStorePort;

@Component
public class InMemoryDowJonesDocumentStoreAdapter
        implements DowJonesDocumentStorePort {

    private static final String CAPABILITY =
            "DOW_JONES_DOCUMENT_STORE";

    private final Map<UUID, Entry> documents =
            new ConcurrentHashMap<>();

    private final DowJonesProperties properties;

    public InMemoryDowJonesDocumentStoreAdapter(
            DowJonesProperties properties
    ) {
        this.properties = properties;
    }

    @Override
    public StoredDowJonesDocument store(
            UUID askId,
            GeneratedDowJonesDocument document
    ) {
        cleanupExpired();

        byte[] content = document.content();

        if (content.length > properties.getMaxPdfBytes()) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Dow Jones PDF exceeds the configured maximum size."
            );
        }

        Instant expiresAt =
                Instant.now().plus(
                        properties.getDocumentTtl()
                );

        String sha256 =
                calculateSha256(content);

        Entry entry = new Entry(
                document.fileName(),
                document.contentType(),
                content,
                sha256,
                expiresAt
        );

        documents.put(askId, entry);

        return new StoredDowJonesDocument(
                askId,
                entry.fileName(),
                entry.contentType(),
                entry.content().length,
                entry.sha256(),
                entry.expiresAt()
        );
    }

    @Override
    public Optional<DowJonesDocumentContent> find(
            UUID askId
    ) {
        cleanupExpired();

        Entry entry = documents.get(askId);

        if (entry == null) {
            return Optional.empty();
        }

        if (!entry.expiresAt().isAfter(
                Instant.now()
        )) {
            documents.remove(askId);
            return Optional.empty();
        }

        return Optional.of(
                new DowJonesDocumentContent(
                        askId,
                        entry.fileName(),
                        entry.contentType(),
                        entry.content(),
                        entry.expiresAt()
                )
        );
    }

    private void cleanupExpired() {
        Instant now = Instant.now();

        documents.entrySet()
                .removeIf(entry ->
                        !entry
                                .getValue()
                                .expiresAt()
                                .isAfter(now)
                );
    }

    private String calculateSha256(
            byte[] content
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat
                    .of()
                    .formatHex(
                            digest.digest(content)
                    );

        } catch (
                NoSuchAlgorithmException exception
        ) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SHA-256 is not available.",
                    exception
            );
        }
    }

    private record Entry(
            String fileName,
            String contentType,
            byte[] content,
            String sha256,
            Instant expiresAt
    ) {
        private Entry {
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
}
