package ro.bcrleasing.leasingdecisioncore.capability.company;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.CompanyDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.CompanyData;

@Repository
public class CompanyDatabaseAdapter implements CompanyDataPort {

    private static final String FIND_BY_CUI_SQL = """
            SELECT c.id,
                   c.cui,
                   c.name
              FROM companies c
             WHERE c.cui = :cui
             ORDER BY c.id
             LIMIT 1
            """;

    private static final String FIND_BY_LEASE_ID_AND_COMPANY_ID_SQL = """
            SELECT c.id,
                   c.cui,
                   c.name
              FROM companies c
              JOIN leases l
                ON l.company_id = c.id
             WHERE l.id = :leaseId
               AND c.id = :companyId
            """;

    private static final RowMapper<CompanyData> COMPANY_ROW_MAPPER =
            (resultSet, rowNumber) ->
                    new CompanyData(resultSet.getLong("id"),
                            resultSet.getString("cui"),
                            resultSet.getString("name"));

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CompanyDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CompanyData> findByCui(String cui) {
        List<CompanyData> results = jdbcTemplate.query(FIND_BY_CUI_SQL, Map.of("cui", cui), COMPANY_ROW_MAPPER);

        return results.stream().findFirst();
    }

    @Override
    public Optional<CompanyData> findByLeaseIdAndCompanyId(long leaseId, long companyId) {

        List<CompanyData> results = jdbcTemplate.query(
                FIND_BY_LEASE_ID_AND_COMPANY_ID_SQL,
                Map.of("leaseId", leaseId, "companyId", companyId),
                COMPANY_ROW_MAPPER);

        return results.stream().findFirst();
    }
}