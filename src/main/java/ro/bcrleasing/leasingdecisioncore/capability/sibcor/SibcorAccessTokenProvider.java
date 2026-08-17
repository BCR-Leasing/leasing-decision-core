package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class SibcorAccessTokenProvider {

    private static final String CAPABILITY =
            "SIBCOR_AUTH";

    private final RestClient restClient;
    private final SibcorProperties properties;
    private final Clock clock;
    private volatile CachedToken cachedToken;

    @Autowired
    public SibcorAccessTokenProvider(
            @Qualifier("sibcorRestClient")
            RestClient restClient,
            SibcorProperties properties
    ) {
        this(
                restClient,
                properties,
                Clock.systemUTC()
        );
    }

    SibcorAccessTokenProvider(
            RestClient restClient,
            SibcorProperties properties,
            Clock clock
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.clock = clock;
    }

    public String getAccessToken() {
        CachedToken current = cachedToken;

        if (isValid(current)) {
            return current.value();
        }

        synchronized (this) {
            current = cachedToken;

            if (isValid(current)) {
                return current.value();
            }

            cachedToken = requestNewToken();

            return cachedToken.value();
        }
    }

    public synchronized void invalidate() {
        cachedToken = null;
    }

    private CachedToken requestNewToken() {
        URI authUri = UriComponentsBuilder
                .fromUriString(
                        properties.getAuthBaseUrl()
                )
                .queryParam(
                        "client_id",
                        properties.getAuthClientId()
                )
                .queryParam(
                        "grant_type",
                        properties.getAuthGrantType()
                )
                .build()
                .encode()
                .toUri();

        try {
            SibcorTokenResponse response =
                    restClient
                            .post()
                            .uri(authUri)
                            .headers(headers ->
                                    headers.setBasicAuth(
                                            properties.getAuthUsername(),
                                            properties.getAuthPassword(),
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .accept(
                                    MediaType.APPLICATION_JSON
                            )
                            .retrieve()
                            .body(
                                    SibcorTokenResponse.class
                            );

            if (response == null || !StringUtils.hasText(response.accessToken())) {

                throw new ExternalCapabilityException(
                        CAPABILITY,
                        "SIBCOR authentication returned an empty access token."
                );
            }

            Duration tokenTtl =
                    response.expiresIn() != null
                            && response.expiresIn() > 0
                            ? Duration.ofSeconds(
                            response.expiresIn()
                    )
                            : properties
                            .getFallbackTokenTtl();

            Instant expiresAt =
                    Instant.now(clock).plus(tokenTtl);

            return new CachedToken(
                    response.accessToken(),
                    expiresAt
            );
        } catch (
                RestClientResponseException exception
        ) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR authentication returned HTTP status "
                            + exception
                            .getStatusCode()
                            .value()
                            + ".",
                    exception
            );
        } catch (RestClientException exception) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "Could not obtain the SIBCOR access token.", exception
            );
        }
    }

    private boolean isValid(CachedToken token) {
        if (token == null) {
            return false;
        }

        Instant refreshThreshold =
                Instant.now(clock).plus(
                        properties.getTokenRefreshBeforeExpiry()
                );

        return token.expiresAt()
                .isAfter(refreshThreshold);
    }

    private record CachedToken(
            String value,
            Instant expiresAt
    ) {
    }
}