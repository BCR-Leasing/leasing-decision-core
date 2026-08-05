package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "decision-core.capabilities.sibcor")
public class SibcorProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String blacklistPath;

    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(15);
    private String username;
    private String password;
    private Map<String, String> headers = new LinkedHashMap<>();

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(headers);
    }
}
