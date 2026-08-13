package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.SibcorPort;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import tools.jackson.databind.JsonNode;

import java.math.BigInteger;
import java.net.URI;

@Component
public class SibcorHttpAdapter implements SibcorPort {

    private static final String CAPABILITY = "SIBCOR";

    private final RestClient restClient;
    private final SibcorProperties properties;
    private final SibcorAccessTokenProvider tokenProvider;
    private final SibcorResponseMapper responseMapper;

    public SibcorHttpAdapter(@Qualifier("sibcorRestClient") RestClient restClient, SibcorProperties properties, SibcorAccessTokenProvider tokenProvider, SibcorResponseMapper responseMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.tokenProvider = tokenProvider;
        this.responseMapper = responseMapper;
    }

    @Override
    public SibcorFacts checkBlacklist(BlacklistSubject subject) {
        String cnpCui = normalizeIdentifier(
                subject.identifier()
        );

        String accessToken =
                tokenProvider.getAccessToken();

        try {
            JsonNode response = executeRequest(
                    cnpCui,
                    subject.name(),
                    accessToken
            );

            return responseMapper.map(response);
        } catch (
                RestClientResponseException exception
        ) {

            if (exception.getStatusCode().value() == 401) {
                tokenProvider.invalidate();

                return retryWithNewToken(
                        cnpCui,
                        subject.name()
                );
            }

            throw toCapabilityException(exception);
        } catch (RestClientException exception) {
            throw new ExternalCapabilityException(CAPABILITY, "SIBCOR blacklist call failed.", exception);
        }
    }

    private SibcorFacts retryWithNewToken(String cnpCui, String clientName) {
        try {
            JsonNode response = executeRequest(
                    cnpCui,
                    clientName,
                    tokenProvider.getAccessToken()
            );

            return responseMapper.map(response);
        } catch (
                RestClientResponseException exception
        ) {
            throw toCapabilityException(exception);
        } catch (RestClientException exception) {
            throw new ExternalCapabilityException(CAPABILITY, "SIBCOR blacklist retry failed.", exception);
        }
    }

    private JsonNode executeRequest(String cnpCui, String clientName, String accessToken) {

        URI requestUri = buildBlacklistUri(cnpCui, clientName);

        RestClient.RequestHeadersSpec<?> requestSpec =
                restClient
                        .get()
                        .uri(requestUri);

        requestSpec.headers(headers -> headers.setBearerAuth(accessToken));

        return requestSpec
                .retrieve()
                .body(JsonNode.class);
    }

    private URI buildBlacklistUri(String cnpCui, String clientName) {
        String endpointUrl =
                removeTrailingSlash(properties.getBaseUrl())
                        + "/"
                        + removeLeadingSlash(
                        properties.getBlacklistPath()
                );

        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromUriString(endpointUrl)
                        .queryParam(
                                "cnpCui",
                                cnpCui
                        );

        if (StringUtils.hasText(clientName)) {
            builder.queryParam(
                    "clientName",
                    clientName
            );
        }

        return builder
                .build()
                .encode()
                .toUri();
    }

    private String normalizeIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            throw new InvalidSubjectDataException("The identifier is required for the SIBCOR call.");
        }

        String trimmed = identifier.trim();

        if (!trimmed.matches("\\d+")) {
            throw new InvalidSubjectDataException("The SIBCOR identifier must contain only digits.");
        }

        return new BigInteger(trimmed).toString();
    }

    private ExternalCapabilityException toCapabilityException(RestClientResponseException exception) {
        return new ExternalCapabilityException(
                CAPABILITY,
                "SIBCOR returned HTTP status "
                        + exception
                        .getStatusCode()
                        .value()
                        + ".",
                exception
        );
    }

    private String removeTrailingSlash(String value) {
        return value.replaceFirst("/+$", "");
    }

    private String removeLeadingSlash(String value) {
        return value.replaceFirst("^/+", "");
    }
}