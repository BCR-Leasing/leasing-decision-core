package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import java.util.Optional;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.CompanyData;

public interface CompanyDataPort {

    Optional<CompanyData> findByLeaseIdAndCompanyId(
            long leaseId,
            long companyId
    );
}
