package ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model;

public record CompanyData(
        long id,
        String cui,
        String name
) {
}
