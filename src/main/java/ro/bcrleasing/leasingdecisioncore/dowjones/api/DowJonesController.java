package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;
import ro.bcrleasing.leasingdecisioncore.dowjones.application.DowJonesAskOrchestrator;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesDocumentStorePort;

@RestController
@RequestMapping("/api/v1/dow-jones/asks")
@Tag(
        name = "Dow Jones",
        description = "Dow Jones screening operations"
)
@SecurityRequirement(name = "basicAuth")
public class DowJonesController {

    private final DowJonesAskOrchestrator askOrchestrator;
    private final DowJonesApiMapper apiMapper;
    private final DowJonesDocumentStorePort documentStorePort;

    public DowJonesController(
            DowJonesAskOrchestrator askOrchestrator,
            DowJonesApiMapper apiMapper,
            DowJonesDocumentStorePort documentStorePort
    ) {
        this.askOrchestrator = askOrchestrator;
        this.apiMapper = apiMapper;
        this.documentStorePort = documentStorePort;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Execute a Dow Jones screening",
            description = """
                    Loads the company from the eBCRL database, executes the
                    Dow Jones robot, captures all result pages and generates
                    the PDF returned by the platform.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Screening completed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Lease or company not found"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Dow Jones capability is unavailable"
            )
    })
    public DowJonesAskResponse screen(
            @Valid @RequestBody DowJonesAskRequest request
    ) {
        return apiMapper.toResponse(
                askOrchestrator.execute(
                        apiMapper.toCommand(request)
                )
        );
    }

    @GetMapping(
            path = "/{askId}/document",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @Operation(
            summary = "Download the generated Dow Jones PDF"
    )
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable UUID askId
    ) {
        var document = documentStorePort
                .find(askId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Dow Jones document does not exist or has expired."
                        )
                );

        String contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(document.fileName())
                        .build()
                        .toString();

        byte[] content = document.content();

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.parseMediaType(
                                document.contentType()
                        )
                )
                .contentLength(content.length)
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .body(content);
    }
}
