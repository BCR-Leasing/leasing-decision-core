package ro.bcrleasing.leasingdecisioncore.dowjones.api;

public record DowJonesMatchResponse(
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
