package ro.bcrleasing.leasingdecisioncore.capability.negativeinformation;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.InternalNegativeInformationFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.RestrictedEntitiesPort;

@Repository
public class RestrictedEntitiesDatabaseAdapter
        implements RestrictedEntitiesPort {

    private static final String SQL = """
            SELECT *
              FROM restricted_entities
             WHERE identifier = :identifier
             LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RestrictedEntitiesDatabaseAdapter(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public InternalNegativeInformationFacts findByIdentifier(
            String identifier
    ) {
        List<Map<String, Object>> results = jdbcTemplate.query(
                SQL,
                Map.of("identifier", identifier),
                new ColumnMapRowMapper()
        );

        return results.stream()
                .findFirst()
                .<InternalNegativeInformationFacts>map(
                        InternalNegativeInformationFacts::match
                )
                .orElseGet(
                        InternalNegativeInformationFacts::noMatch
                );
    }
}
