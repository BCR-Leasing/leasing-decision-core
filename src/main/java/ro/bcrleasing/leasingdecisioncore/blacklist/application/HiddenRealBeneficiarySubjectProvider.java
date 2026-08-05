package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.RealBeneficiaryDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.RealBeneficiaryData;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;

@Component
public class HiddenRealBeneficiarySubjectProvider
        implements BlacklistSubjectProvider {

    private final RealBeneficiaryDataPort realBeneficiaryDataPort;

    public HiddenRealBeneficiarySubjectProvider(
            RealBeneficiaryDataPort realBeneficiaryDataPort
    ) {
        this.realBeneficiaryDataPort = realBeneficiaryDataPort;
    }

    @Override
    public SubjectType supportedType() {
        return SubjectType.HIDDEN_REAL_BENEFICIARY;
    }

    @Override
    public BlacklistSubject loadSubject(long leaseId, String subjectId) {
        if (subjectId == null || subjectId.isBlank()) {
            throw new InvalidSubjectDataException(
                    "For HIDDEN_REAL_BENEFICIARY, subjectId must contain the beneficiary identifier."
            );
        }

        RealBeneficiaryData beneficiary = realBeneficiaryDataPort
                .findByLeaseIdAndIdentifier(leaseId, subjectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The real beneficiary was not found in the legacy KO details."
                ));

        return new BlacklistSubject(
                SubjectType.HIDDEN_REAL_BENEFICIARY,
                beneficiary.sourceId(),
                beneficiary.identifier(),
                beneficiary.name(),
                "Beneficiar Real"
        );
    }
}
