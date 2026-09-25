package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import java.net.URI;

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

@Component
public class SibcorHttpAdapter
        implements SibcorPort {

    private static final String CAPABILITY = "SIBCOR";

    private final RestClient restClient;
    private final SibcorProperties properties;
    private final SibcorAccessTokenProvider tokenProvider;
    private final SibcorResponseMapper responseMapper;

    public SibcorHttpAdapter(
            @Qualifier("sibcorRestClient")
            RestClient restClient,
            SibcorProperties properties,
            SibcorAccessTokenProvider tokenProvider,
            SibcorResponseMapper responseMapper
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.tokenProvider = tokenProvider;
        this.responseMapper = responseMapper;
    }

    @Override
    public SibcorFacts checkBlacklist(
            BlacklistSubject subject
    ) {
        String cnpCui =
                normalizeIdentifier(
                        subject.identifier()
                );

        String clientName =
                normalizeClientName(
                        subject.name()
                );

        String accessToken =
                tokenProvider.getAccessToken();

        try {
            JsonNode response =
                    executeRequest(
                            cnpCui,
                            clientName,
                            accessToken
                    );

            return responseMapper.map(
                    response,
                    cnpCui
            );

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 401) {
                tokenProvider.invalidate();

                return retryWithNewToken(
                        cnpCui,
                        clientName
                );
            }

            throw toCapabilityException(exception);

        } catch (RestClientException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR blacklist call failed.",
                    exception
            );
        }
    }

    private SibcorFacts retryWithNewToken(
            String cnpCui,
            String clientName
    ) {
        try {
            JsonNode response =
                    executeRequest(
                            cnpCui,
                            clientName,
                            tokenProvider.getAccessToken()
                    );

            return responseMapper.map(
                    response,
                    cnpCui
            );

        } catch (RestClientResponseException exception) {
            throw toCapabilityException(exception);

        } catch (RestClientException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR blacklist retry failed.",
                    exception
            );
        }
    }

    private JsonNode executeRequest(
            String cnpCui,
            String clientName,
            String accessToken
    ) {
        URI requestUri =
                buildBlacklistUri(
                        cnpCui,
                        clientName
                );

        return restClient
                .get()
                .uri(requestUri)
                .headers(headers ->
                        headers.setBearerAuth(accessToken)
                )
                .retrieve()
                .body(JsonNode.class);
    }

    private URI buildBlacklistUri(
            String cnpCui,
            String clientName
    ) {
        String endpointUrl =
                removeTrailingSlash(
                        properties.getBaseUrl()
                )
                        + "/"
                        + removeLeadingSlash(
                                properties.getBlacklistPath()
                        );

        return UriComponentsBuilder
                .fromUriString(endpointUrl)
                .queryParam(
                        "cnpCui",
                        cnpCui
                )
                .queryParam(
                        "clientName",
                        clientName
                )
                .build()
                .encode()
                .toUri();
    }

    private String normalizeIdentifier(
            String identifier
    ) {
        if (!StringUtils.hasText(identifier)) {
            throw new InvalidSubjectDataException(
                    "The identifier is required for the SIBCOR call."
            );
        }

        String normalized = identifier.trim();

        if (!normalized.matches("\\d+")) {
            throw new InvalidSubjectDataException(
                    "The SIBCOR identifier must contain only digits."
            );
        }

        return normalized;
    }

    private String normalizeClientName(
            String clientName
    ) {
        if (!StringUtils.hasText(clientName)) {
            throw new InvalidSubjectDataException(
                    "The client name is required for the SIBCOR call."
            );
        }

        return clientName.trim();
    }

    private ExternalCapabilityException toCapabilityException(
            RestClientResponseException exception
    ) {
        return new ExternalCapabilityException(
                CAPABILITY,
                "SIBCOR returned HTTP status "
                        + exception.getStatusCode().value()
                        + ".",
                exception
        );
    }

    private String removeTrailingSlash(
            String value
    ) {
        return value.replaceFirst(
                "/+$",
                ""
        );
    }

    private String removeLeadingSlash(
            String value
    ) {
        return value.replaceFirst(
                "^/+",
                ""
        );
    }
}
