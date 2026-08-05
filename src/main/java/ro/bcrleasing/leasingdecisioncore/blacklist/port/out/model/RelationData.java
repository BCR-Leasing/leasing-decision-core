package ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model;

public record RelationData(
        long id,
        long companyId,
        String identifier,
        int relationCode
) {
}
