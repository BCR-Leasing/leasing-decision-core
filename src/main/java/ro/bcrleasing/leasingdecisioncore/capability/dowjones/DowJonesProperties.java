package ro.bcrleasing.leasingdecisioncore.capability.dowjones;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "decision-core.capabilities.dow-jones"
)
public class DowJonesProperties {

    private String url = "https://riskcenter.dowjones.com/dashboard";
    private String documentDirectory;
    private String username;
    private String password;
    private boolean headless = true;
    private boolean ignoreHttpsErrors = false;
    private String browserExecutablePath;
    private double slowMoMs = 0;
    private Duration navigationTimeout = Duration.ofSeconds(30);
    private Duration searchTimeout = Duration.ofSeconds(60);
    private Duration downloadTimeout = Duration.ofSeconds(60);
    private Duration documentTtl = Duration.ofMinutes(60);
    private int maxConcurrentScreenings = 1;
    private int maxPages = 50;
    private long maxPdfBytes = 25L * 1024L * 1024L;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public boolean isIgnoreHttpsErrors() {
        return ignoreHttpsErrors;
    }

    public void setIgnoreHttpsErrors(
            boolean ignoreHttpsErrors
    ) {
        this.ignoreHttpsErrors = ignoreHttpsErrors;
    }

    public String getBrowserExecutablePath() {
        return browserExecutablePath;
    }

    public void setBrowserExecutablePath(
            String browserExecutablePath
    ) {
        this.browserExecutablePath =
                browserExecutablePath;
    }

    public double getSlowMoMs() {
        return slowMoMs;
    }

    public void setSlowMoMs(double slowMoMs) {
        this.slowMoMs = slowMoMs;
    }

    public Duration getNavigationTimeout() {
        return navigationTimeout;
    }

    public void setNavigationTimeout(
            Duration navigationTimeout
    ) {
        this.navigationTimeout = navigationTimeout;
    }

    public Duration getSearchTimeout() {
        return searchTimeout;
    }

    public void setSearchTimeout(
            Duration searchTimeout
    ) {
        this.searchTimeout = searchTimeout;
    }

    public Duration getDownloadTimeout() {
        return downloadTimeout;
    }

    public void setDownloadTimeout(
            Duration downloadTimeout
    ) {
        this.downloadTimeout = downloadTimeout;
    }

    public Duration getDocumentTtl() {
        return documentTtl;
    }

    public void setDocumentTtl(
            Duration documentTtl
    ) {
        this.documentTtl = documentTtl;
    }

    public int getMaxConcurrentScreenings() {
        return maxConcurrentScreenings;
    }

    public void setMaxConcurrentScreenings(
            int maxConcurrentScreenings
    ) {
        this.maxConcurrentScreenings =
                maxConcurrentScreenings;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public long getMaxPdfBytes() {
        return maxPdfBytes;
    }

    public void setMaxPdfBytes(
            long maxPdfBytes
    ) {
        this.maxPdfBytes = maxPdfBytes;
    }

    public String getDocumentDirectory() {
        return documentDirectory;
    }

    public void setDocumentDirectory(String documentDirectory) {
        this.documentDirectory = documentDirectory;
    }

}
