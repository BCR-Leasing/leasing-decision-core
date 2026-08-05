package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import java.util.Optional;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.KycData;

public interface KycDataPort {

    Optional<KycData> findByLeaseIdAndKycId(
            long leaseId,
            long kycId
    );
}
