package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import java.util.Optional;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.RelationData;

public interface RelationDataPort {

    Optional<RelationData> findFirstByCompanyIdAndIdentifier(
            long companyId,
            String identifier
    );
}
