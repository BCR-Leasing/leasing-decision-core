package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.GeneratedDowJonesDocument;

@Component
public class DowJonesFileStorage {

    private static final String CAPABILITY = "DOW_JONES";

    private final DowJonesProperties properties;

    public DowJonesFileStorage(DowJonesProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties is required");
    }

    public String save(GeneratedDowJonesDocument document) {
        Objects.requireNonNull(document, "document is required");
        Path temporaryFile = null;

        try {
            String configuredDirectory = properties.getDocumentDirectory();
            if (configuredDirectory == null || configuredDirectory.isBlank()) {
                throw error("Dow Jones document-directory is not configured.");
            }

            Path directory = Path.of(configuredDirectory.trim()).normalize();
            if (!directory.isAbsolute()) {
                throw error("Dow Jones document-directory must be an absolute path.");
            }

            byte[] content = document.content();
            if (content.length == 0) {
                throw error("The generated Dow Jones PDF is empty.");
            }
            if (content.length > properties.getMaxPdfBytes()) {
                throw error("Dow Jones PDF exceeds the configured maximum size.");
            }

            String fileName = uniquePdfName(document.fileName());

            Files.createDirectories(directory);
            directory = directory.toRealPath();

            Path target = directory.resolve(fileName).normalize();
            if (!directory.equals(target.getParent())) {
                throw error("Invalid Dow Jones document file name.");
            }

            temporaryFile = Files.createTempFile(directory, ".dow-jones-", ".part");
            Files.write(
                    temporaryFile,
                    content,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            Files.move(temporaryFile, target);
            temporaryFile = null;

            return target.toString();
        } catch (IOException | RuntimeException exception) {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException | RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }

            if (exception instanceof ExternalCapabilityException capabilityException) {
                throw capabilityException;
            }

            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Could not save the Dow Jones PDF in the configured directory.",
                    exception
            );
        }
    }

    private String uniquePdfName(String originalFileName) {
        if (originalFileName == null
                || !originalFileName.matches("[A-Za-z0-9][A-Za-z0-9._-]*\\.[pP][dD][fF]")) {
            throw error("Dow Jones document file name must be a simple PDF file name.");
        }

        String stem = originalFileName.substring(0, originalFileName.length() - 4);
        if (stem.length() > 120) {
            stem = stem.substring(0, 120);
        }

        return stem + "-" + UUID.randomUUID() + ".pdf";
    }

    private ExternalCapabilityException error(String message) {
        return new ExternalCapabilityException(CAPABILITY, message);
    }
}
