package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.KycDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.KycData;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;

@Component
public class AdministratorSubjectProvider
        implements BlacklistSubjectProvider {

    private final KycDataPort kycDataPort;

    public AdministratorSubjectProvider(KycDataPort kycDataPort) {
        this.kycDataPort = kycDataPort;
    }

    @Override
    public SubjectType supportedType() {
        return SubjectType.ADMINISTRATOR;
    }

    @Override
    public BlacklistSubject loadSubject(long leaseId, String subjectId) {
        long kycId = SubjectIdParser.parseLong(
                subjectId,
                SubjectType.ADMINISTRATOR
        );

        KycData kyc = kycDataPort
                .findByLeaseIdAndKycId(leaseId, kycId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KYC record does not exist or does not belong to the lease."
                ));

        if (kyc.userId() == null) {
            throw new InvalidSubjectDataException(
                    "The selected KYC record is not the administrator/signee KYC."
            );
        }

        String name = joinName(
                kyc.ocrFirstName(),
                kyc.ocrLastName()
        );

        if (name.isBlank()) {
            name = "Unknown Administrator";
        }

        return new BlacklistSubject(
                SubjectType.ADMINISTRATOR,
                Long.toString(kyc.id()),
                kyc.ocrCnp(),
                name,
                "Administrator"
        );
    }

    private String joinName(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (first + " " + last).trim();
    }
}
