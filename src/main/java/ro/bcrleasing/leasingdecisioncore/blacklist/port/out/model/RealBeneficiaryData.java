package ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model;

public record RealBeneficiaryData(
        String sourceId,
        String identifier,
        String name
) {
}
