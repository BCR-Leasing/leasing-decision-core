package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import java.util.Optional;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.RealBeneficiaryData;

public interface RealBeneficiaryDataPort {

    Optional<RealBeneficiaryData> findByLeaseIdAndIdentifier(
            long leaseId,
            String identifier
    );
}
