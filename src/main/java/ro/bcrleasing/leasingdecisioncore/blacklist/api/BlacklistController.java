package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.bcrleasing.leasingdecisioncore.ask.application.AskOrchestrator;

@RestController
@RequestMapping(path = "/api/v1/blacklist/asks", produces = MediaType.APPLICATION_JSON_VALUE)
public class BlacklistController {
    private final AskOrchestrator askOrchestrator;
    private final BlacklistApiMapper apiMapper;

    public BlacklistController(AskOrchestrator askOrchestrator, BlacklistApiMapper apiMapper) {
        this.askOrchestrator = askOrchestrator;
        this.apiMapper = apiMapper;
    }

    @Operation(summary = "Execute blacklist")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Company blacklist",
                            value = """
            {
              "requestId": "574f9595-5e19-4a46-9f5d-27ba22e695a2",
              "leaseId": 8987,
              "subjectType": "COMPANY",
              "subjectId": "1151"
            }
            """
                    )
            )
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public BlacklistDecisionResponse checkBlacklist(@Valid @RequestBody BlacklistAskRequest request) {
        return apiMapper.toResponse(
                askOrchestrator.executeBlacklist(
                        apiMapper.toCommand(request)
                )
        );
    }
}
