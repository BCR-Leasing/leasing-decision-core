package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BlacklistAskRequest(

        @JsonProperty("cnpCUI")
        @JsonAlias("cnpCui")
        @Pattern(regexp = "\\d+", message = "cnpCUI must contain only digits")
        String cnpCui,
        String clientName,
        List<@NotNull(message = "queries cannot contain null entries") @Valid BlacklistQueryRequest> queries) {

    public BlacklistAskRequest {
        cnpCui = normalizeOptionalValue(cnpCui);
        clientName = normalizeOptionalValue(clientName);

        queries = queries == null ? null : Collections.unmodifiableList(new ArrayList<>(queries));
    }

    @AssertTrue(message = "Provide either cnpCUI with an optional clientName, or a non-empty queries collection, but not both")
    @JsonIgnore
    public boolean isValidRequestShape() {
        boolean singleRequest = hasText(cnpCui);
        boolean queriesProvided = queries != null;
        boolean batchRequest = queriesProvided && !queries.isEmpty();

        if (singleRequest) {
            return !queriesProvided;
        }

        if (hasText(clientName)) {
            return false;
        }

        return batchRequest;
    }

    @JsonIgnore
    public List<BlacklistQueryRequest> effectiveQueries() {
        if (hasText(cnpCui)) {
            return List.of(new BlacklistQueryRequest(cnpCui, clientName));
        }

        return queries == null ? List.of() : queries;
    }

    private static String normalizeOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
