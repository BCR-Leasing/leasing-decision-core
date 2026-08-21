package ro.bcrleasing.leasingdecisioncore.dowjones.domain;

public record DowJonesMatch(
        String profileId,
        String name,
        String gender,
        String dateOfBirth,
        String country,
        String details,
        String subsidiary,
        String score
) {
}
