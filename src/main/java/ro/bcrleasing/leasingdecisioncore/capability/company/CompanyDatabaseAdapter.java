package ro.bcrleasing.leasingdecisioncore.capability.company;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.CompanyDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.CompanyData;

@Repository
public class CompanyDatabaseAdapter implements CompanyDataPort {

    private static final String SQL = """
            SELECT c.id,
                   c.cui,
                   c.name
              FROM companies c
              JOIN leases l
                ON l.company_id = c.id
             WHERE l.id = :leaseId
               AND c.id = :companyId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CompanyDatabaseAdapter(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CompanyData> findByLeaseIdAndCompanyId(
            long leaseId,
            long companyId
    ) {
        List<CompanyData> results = jdbcTemplate.query(
                SQL,
                Map.of(
                        "leaseId", leaseId,
                        "companyId", companyId
                ),
                (resultSet, rowNumber) -> new CompanyData(
                        resultSet.getLong("id"),
                        resultSet.getString("cui"),
                        resultSet.getString("name")
                )
        );

        return results.stream().findFirst();
    }
}
