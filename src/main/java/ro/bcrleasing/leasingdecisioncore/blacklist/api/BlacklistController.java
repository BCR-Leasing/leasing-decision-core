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
@RequestMapping(
        path = "/api/v1/blacklist/asks",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class BlacklistController {

    private final AskOrchestrator askOrchestrator;
    private final BlacklistApiMapper apiMapper;

    public BlacklistController(
            AskOrchestrator askOrchestrator,
            BlacklistApiMapper apiMapper
    ) {
        this.askOrchestrator = askOrchestrator;
        this.apiMapper = apiMapper;
    }

    @Operation(
            summary = "Execute one or more blacklist queries"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = {
                            @ExampleObject(
                                    name = "Single query with client name",
                                    value = """
                                            {
                                              "cnpCUI": "45589918",
                                              "clientName": "CIORBARIA LUI ANTONIO SRL"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Dow Jones verification example",
                                    value = """
                                            {
                                              "cnpCUI": "28822508",
                                              "clientName": "MAXIM CARGO SRL"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Single query without client name",
                                    value = """
                                            {
                                              "cnpCUI": "45589918"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Multiple mixed queries",
                                    value = """
                                            {
                                              "queries": [
                                                {
                                                  "cnpCUI": "45589918",
                                                  "clientName": "CIORBARIA LUI ANTONIO SRL"
                                                },
                                                {
                                                  "cnpCUI": "41337179"
                                                },
                                                {
                                                  "cnpCUI": "32608813",
                                                  "clientName": "MARAMANDA SRL"
                                                }
                                              ]
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public BlacklistDecisionResponse checkBlacklist(
            @Valid
            @RequestBody
            BlacklistAskRequest request
    ) {
        return apiMapper.toResponse(
                askOrchestrator.executeBlacklist(
                        apiMapper.toCommand(request)
                )
        );
    }
}
