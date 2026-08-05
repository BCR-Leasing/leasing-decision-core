package ro.bcrleasing.leasingdecisioncore.capability.negativeinformation;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.InternalNegativeInformationFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.NegativeInformationPort;

@Repository
public class NegativeInformationDatabaseAdapter
        implements NegativeInformationPort {

    private static final String SQL = """
            SELECT *
              FROM catalog_parteneri_informatii_negative
             WHERE identificator = :identifier
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public NegativeInformationDatabaseAdapter(
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
