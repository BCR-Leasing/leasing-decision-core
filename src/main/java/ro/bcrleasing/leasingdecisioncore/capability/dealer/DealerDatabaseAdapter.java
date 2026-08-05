package ro.bcrleasing.leasingdecisioncore.capability.dealer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.DealerDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.DealerData;

@Repository
public class DealerDatabaseAdapter implements DealerDataPort {

    private static final String SQL = """
            SELECT d.id,
                   d.cui,
                   d.name
              FROM dealers d
              JOIN leases l
                ON l.dealer_id = d.id
             WHERE l.id = :leaseId
               AND d.id = :dealerId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DealerDatabaseAdapter(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<DealerData> findByLeaseIdAndDealerId(
            long leaseId,
            long dealerId
    ) {
        List<DealerData> results = jdbcTemplate.query(
                SQL,
                Map.of(
                        "leaseId", leaseId,
                        "dealerId", dealerId
                ),
                (resultSet, rowNumber) -> new DealerData(
                        resultSet.getLong("id"),
                        resultSet.getString("cui"),
                        resultSet.getString("name")
                )
        );

        return results.stream().findFirst();
    }
}
