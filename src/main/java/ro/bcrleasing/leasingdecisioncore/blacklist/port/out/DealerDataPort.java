package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import java.util.Optional;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.DealerData;

public interface DealerDataPort {

    Optional<DealerData> findByLeaseIdAndDealerId(
            long leaseId,
            long dealerId
    );
}
