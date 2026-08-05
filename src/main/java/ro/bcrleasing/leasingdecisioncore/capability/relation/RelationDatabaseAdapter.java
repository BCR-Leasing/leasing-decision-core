package ro.bcrleasing.leasingdecisioncore.capability.relation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.RelationDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.RelationData;

@Repository
public class RelationDatabaseAdapter implements RelationDataPort {

    private static final String SQL = """
            SELECT id,
                   company_id,
                   identifier,
                   relation
              FROM relations
             WHERE company_id = :companyId
               AND identifier = :identifier
             ORDER BY id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RelationDatabaseAdapter(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RelationData> findFirstByCompanyIdAndIdentifier(
            long companyId,
            String identifier
    ) {
        List<RelationData> results = jdbcTemplate.query(
                SQL,
                Map.of(
                        "companyId", companyId,
                        "identifier", identifier
                ),
                (resultSet, rowNumber) -> new RelationData(
                        resultSet.getLong("id"),
                        resultSet.getLong("company_id"),
                        resultSet.getString("identifier"),
                        resultSet.getInt("relation")
                )
        );

        return results.stream().findFirst();
    }
}
