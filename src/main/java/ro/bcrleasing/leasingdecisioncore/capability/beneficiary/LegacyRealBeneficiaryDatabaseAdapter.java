package ro.bcrleasing.leasingdecisioncore.capability.beneficiary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.RealBeneficiaryDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.RealBeneficiaryData;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Repository
public class LegacyRealBeneficiaryDatabaseAdapter
        implements RealBeneficiaryDataPort {

    private static final String SQL = """
            SELECT kd.real_beneficiary
              FROM kos_details kd
              JOIN leases l
                ON l.ko_id = kd.ko_id
             WHERE l.id = :leaseId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public LegacyRealBeneficiaryDatabaseAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<RealBeneficiaryData> findByLeaseIdAndIdentifier(
            long leaseId,
            String identifier
    ) {
        List<String> payloads = jdbcTemplate.query(
                SQL,
                Map.of("leaseId", leaseId),
                (resultSet, rowNumber) ->
                        resultSet.getString("real_beneficiary")
        );

        return payloads.stream()
                .filter(payload -> payload != null && !payload.isBlank())
                .findFirst()
                .flatMap(payload -> parse(payload, identifier));
    }

    private Optional<RealBeneficiaryData> parse(
            String payload,
            String identifier
    ) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            if (node.isArray() && !node.isEmpty()) {
                node = node.get(0);
            }

            if (node != null && node.isTextual()) {
                node = objectMapper.readTree(node.asText());
            }

            List<JsonNode> candidates = new ArrayList<>();
            if (node != null && node.isArray()) {
                node.forEach(candidates::add);
            } else if (node != null && node.isObject()) {
                candidates.add(node);
            }

            for (JsonNode candidate : candidates) {
                String candidateIdentifier =
                        textOrNull(candidate.get("identifier"));

                if (identifier.equals(candidateIdentifier)) {
                    String name = textOrNull(candidate.get("name"));

                    return Optional.of(
                            new RealBeneficiaryData(
                                    candidateIdentifier,
                                    candidateIdentifier,
                                    name
                            )
                    );
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new InvalidSubjectDataException(
                    "The legacy real_beneficiary payload cannot be parsed.",
                    exception
            );
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isTextual() ? node.textValue() : node.asText();
    }
}
