package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.InternalNegativeInformationFacts;

public interface NegativeInformationPort {

    InternalNegativeInformationFacts findByIdentifier(String identifier);
}
