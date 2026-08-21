package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.springframework.stereotype.Component;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubject;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.GeneratedDowJonesDocument;

@Component
public class DowJonesPdfExtractor {

    private static final String CAPABILITY =
            "DOW_JONES";

    private static final String SEARCH_PDF_ENDPOINT =
            "/search-pdf";

    private static final String PDF_MIME_TYPE =
            "application/pdf";

    private static final String BASE64_ENCODING =
            "base64";

    private static final String DOWNLOAD_BUTTON =
            "svg[aria-label='file-download'][role='button']";

    private static final String PDF_BUTTON =
            "//button[@value='PDF']";

    private static final String DOWNLOAD_CONTINUE_BUTTON =
            "//div[.//h3[normalize-space()='Download']]"
                    + "//button[normalize-space()='Continue']";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DowJonesProperties properties;

    public DowJonesPdfExtractor(
            DowJonesProperties properties
    ) {
        this.properties = properties;
    }

    public GeneratedDowJonesDocument download(
            Page page,
            DowJonesSubject subject
    ) {
        page.setDefaultTimeout(
                properties
                        .getDownloadTimeout()
                        .toMillis()
        );

        page.locator(DOWNLOAD_BUTTON)
                .first()
                .click();

        page.locator(PDF_BUTTON)
                .first()
                .click();

        Response response =
                page.waitForResponse(
                        candidate ->
                                candidate
                                        .url()
                                        .contains(
                                                SEARCH_PDF_ENDPOINT
                                        )
                                        && "POST".equals(
                                                candidate
                                                        .request()
                                                        .method()
                                        ),
                        () ->
                                page.locator(
                                                DOWNLOAD_CONTINUE_BUTTON
                                        )
                                        .first()
                                        .click()
                );

        response.finished();

        if (response.status() != 200) {
            throw error(
                    "Dow Jones PDF request returned HTTP status "
                            + response.status()
                            + "."
            );
        }

        byte[] content = extractPdfBytes(response);

        if (content.length > properties.getMaxPdfBytes()) {
            throw error(
                    "Dow Jones PDF exceeds the configured maximum size."
            );
        }

        if (!looksLikePdf(content)) {
            throw error(
                    "Dow Jones returned content that is not a valid PDF."
            );
        }

        String fileName =
                "dow-jones-"
                        + subject
                        .type()
                        .name()
                        .toLowerCase(Locale.ROOT)
                        + "-"
                        + sanitizeFileName(
                                subject.sourceId()
                        )
                        + ".pdf";

        return new GeneratedDowJonesDocument(
                content,
                PDF_MIME_TYPE,
                fileName
        );
    }

    private byte[] extractPdfBytes(
            Response response
    ) {
        try {
            JsonNode json =
                    objectMapper.readTree(
                            response.text()
                    );

            String mimeType = json
                    .path("mime_type")
                    .asText();

            String encoding = json
                    .path("binary_encoding")
                    .asText();

            String binaryStream = json
                    .path("binary_stream")
                    .asText();

            if (!PDF_MIME_TYPE.equalsIgnoreCase(
                    mimeType
            )) {
                throw error(
                        "Unexpected Dow Jones PDF mime type: "
                                + mimeType
                );
            }

            if (!BASE64_ENCODING.equalsIgnoreCase(
                    encoding
            )) {
                throw error(
                        "Unexpected Dow Jones PDF encoding: "
                                + encoding
                );
            }

            if (binaryStream == null
                    || binaryStream.isBlank()) {
                throw error(
                        "Dow Jones returned an empty PDF."
                );
            }

            String normalized =
                    removeDataUriPrefix(
                            binaryStream.trim()
                    );

            return Base64
                    .getDecoder()
                    .decode(normalized);

        } catch (ExternalCapabilityException exception) {
            throw exception;
        } catch (
                IOException
                        | IllegalArgumentException exception
        ) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Could not parse the Dow Jones PDF response.",
                    exception
            );
        }
    }

    private String removeDataUriPrefix(
            String value
    ) {
        int commaIndex = value.indexOf(',');

        if (value.startsWith("data:")
                && commaIndex >= 0) {
            return value.substring(commaIndex + 1);
        }

        return value;
    }

    private boolean looksLikePdf(byte[] content) {
        return content.length >= 4
                && content[0] == '%'
                && content[1] == 'P'
                && content[2] == 'D'
                && content[3] == 'F';
    }

    private String sanitizeFileName(
            String value
    ) {
        return value
                .trim()
                .replaceAll(
                        "[^a-zA-Z0-9-_]",
                        "_"
                );
    }

    private ExternalCapabilityException error(
            String message
    ) {
        return new ExternalCapabilityException(
                CAPABILITY,
                message
        );
    }
}
