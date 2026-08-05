package ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model;

public record KycData(
        long id,
        long leaseId,
        long companyId,
        Long userId,
        String ocrCnp,
        String ocrFirstName,
        String ocrLastName,
        String title
) {
}
