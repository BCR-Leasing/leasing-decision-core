package ro.bcrleasing.leasingdecisioncore.blacklist.api;

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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public BlacklistDecisionResponse checkBlacklist(@Valid @RequestBody BlacklistAskRequest request) {
        return apiMapper.toResponse(
                askOrchestrator.executeBlacklist(
                        apiMapper.toCommand(request)
                )
        );
    }
}
