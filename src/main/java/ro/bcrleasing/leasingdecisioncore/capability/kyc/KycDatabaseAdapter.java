package ro.bcrleasing.leasingdecisioncore.capability.kyc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.KycDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.KycData;

@Repository
public class KycDatabaseAdapter implements KycDataPort {

    private static final String SQL = """
            SELECT k.id,
                   k.lease_id,
                   l.company_id,
                   k.user_id,
                   k.ocr_cnp,
                   k.ocr_first_name,
                   k.ocr_last_name,
                   k.title
              FROM kycs k
              JOIN leases l
                ON l.id = k.lease_id
             WHERE k.lease_id = :leaseId
               AND k.id = :kycId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public KycDatabaseAdapter(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<KycData> findByLeaseIdAndKycId(
            long leaseId,
            long kycId
    ) {
        List<KycData> results = jdbcTemplate.query(
                SQL,
                Map.of(
                        "leaseId", leaseId,
                        "kycId", kycId
                ),
                (resultSet, rowNumber) -> {
                    Number userIdValue =
                            (Number) resultSet.getObject("user_id");

                    return new KycData(
                            resultSet.getLong("id"),
                            resultSet.getLong("lease_id"),
                            resultSet.getLong("company_id"),
                            userIdValue == null
                                    ? null
                                    : userIdValue.longValue(),
                            resultSet.getString("ocr_cnp"),
                            resultSet.getString("ocr_first_name"),
                            resultSet.getString("ocr_last_name"),
                            resultSet.getString("title")
                    );
                }
        );

        return results.stream().findFirst();
    }
}
