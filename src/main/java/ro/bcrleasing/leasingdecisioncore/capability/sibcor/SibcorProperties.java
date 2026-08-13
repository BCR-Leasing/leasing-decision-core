package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(
        prefix = "decision-core.capabilities.sibcor"
)
public class SibcorProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String blacklistPath = "blacklist";

    @NotBlank
    private String authBaseUrl;

    @NotBlank
    private String authClientId;

    @NotBlank
    private String authGrantType = "client_credentials";

    @NotBlank
    private String authUsername;

    @NotBlank
    private String authPassword;

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(5);

    @NotNull
    private Duration readTimeout = Duration.ofSeconds(30);

    @NotNull
    private Duration tokenRefreshBeforeExpiry =
            Duration.ofMinutes(5);

    @NotNull
    private Duration fallbackTokenTtl =
            Duration.ofMinutes(50);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBlacklistPath() {
        return blacklistPath;
    }

    public void setBlacklistPath(String blacklistPath) {
        this.blacklistPath = blacklistPath;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public void setAuthBaseUrl(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    public String getAuthClientId() {
        return authClientId;
    }

    public void setAuthClientId(String authClientId) {
        this.authClientId = authClientId;
    }

    public String getAuthGrantType() {
        return authGrantType;
    }

    public void setAuthGrantType(String authGrantType) {
        this.authGrantType = authGrantType;
    }

    public String getAuthUsername() {
        return authUsername;
    }

    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getTokenRefreshBeforeExpiry() {
        return tokenRefreshBeforeExpiry;
    }

    public void setTokenRefreshBeforeExpiry(
            Duration tokenRefreshBeforeExpiry
    ) {
        this.tokenRefreshBeforeExpiry =
                tokenRefreshBeforeExpiry;
    }

    public Duration getFallbackTokenTtl() {
        return fallbackTokenTtl;
    }

    public void setFallbackTokenTtl(
            Duration fallbackTokenTtl
    ) {
        this.fallbackTokenTtl = fallbackTokenTtl;
    }
}