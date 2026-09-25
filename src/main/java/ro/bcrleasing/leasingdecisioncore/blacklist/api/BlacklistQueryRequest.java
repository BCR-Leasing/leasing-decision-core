package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BlacklistQueryRequest(

        @JsonProperty("cnpCUI")
        @JsonAlias("cnpCui")
        @NotBlank(message = "cnpCUI is required")
        @Pattern(
                regexp = "\\d+",
                message = "cnpCUI must contain only digits"
        )
        String cnpCui,

        String clientName
) {

    public BlacklistQueryRequest {
        cnpCui = normalizeRequiredValue(cnpCui);
        clientName = normalizeOptionalValue(clientName);
    }

    private static String normalizeRequiredValue(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
    }

    private static String normalizeOptionalValue(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
