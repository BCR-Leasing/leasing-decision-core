package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.SibcorPort;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import tools.jackson.databind.JsonNode;

@Component
public class SibcorHttpAdapter implements SibcorPort {

    private static final String CAPABILITY = "SIBCOR";

    private final RestClient restClient;
    private final SibcorProperties properties;
    private final SibcorResponseMapper responseMapper;

    public SibcorHttpAdapter(
            @Qualifier("sibcorRestClient") RestClient restClient,
            SibcorProperties properties,
            SibcorResponseMapper responseMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.responseMapper = responseMapper;
    }

    @Override
    public SibcorFacts checkBlacklist(BlacklistSubject subject) {
        BigInteger numericIdentifier;

        try {
            numericIdentifier = new BigInteger(subject.identifier());
        } catch (NumberFormatException exception) {
            throw new InvalidSubjectDataException(
                    "The subject identifier must contain only digits for the SIBCOR blacklist call.",
                    exception
            );
        }

        try {
            JsonNode response = restClient.post()
                    .uri(properties.getBlacklistPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(new SibcorHttpRequest(
                            numericIdentifier,
                            subject.name()
                    ))
                    .retrieve()
                    .body(JsonNode.class);

            return responseMapper.map(response);
        } catch (RestClientResponseException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR returned HTTP status "
                            + exception.getStatusCode().value()
                            + ".",
                    exception
            );
        } catch (RestClientException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR call failed.",
                    exception
            );
        }
    }
}
