package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.InternalNegativeInformationFacts;

public interface RestrictedEntitiesPort {

    InternalNegativeInformationFacts findByIdentifier(String identifier);
}
