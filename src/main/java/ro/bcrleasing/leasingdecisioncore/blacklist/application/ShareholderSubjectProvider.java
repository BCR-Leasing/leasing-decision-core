package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.KycDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.RelationDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.KycData;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;

@Component
public class ShareholderSubjectProvider
        implements BlacklistSubjectProvider {

    private final KycDataPort kycDataPort;
    private final RelationDataPort relationDataPort;
    private final LegacyRelationRoleMapper roleMapper;

    public ShareholderSubjectProvider(
            KycDataPort kycDataPort,
            RelationDataPort relationDataPort,
            LegacyRelationRoleMapper roleMapper
    ) {
        this.kycDataPort = kycDataPort;
        this.relationDataPort = relationDataPort;
        this.roleMapper = roleMapper;
    }

    @Override
    public SubjectType supportedType() {
        return SubjectType.SHAREHOLDER;
    }

    @Override
    public BlacklistSubject loadSubject(long leaseId, String subjectId) {
        long kycId = SubjectIdParser.parseLong(
                subjectId,
                SubjectType.SHAREHOLDER
        );

        KycData kyc = kycDataPort
                .findByLeaseIdAndKycId(leaseId, kycId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KYC record does not exist or does not belong to the lease."
                ));

        if (kyc.userId() != null) {
            throw new InvalidSubjectDataException(
                    "The selected KYC record represents the administrator, not a shareholder."
            );
        }

        String role = "-";
        if (kyc.ocrCnp() != null && !kyc.ocrCnp().isBlank()) {
            role = relationDataPort
                    .findFirstByCompanyIdAndIdentifier(
                            kyc.companyId(),
                            kyc.ocrCnp()
                    )
                    .map(relation -> roleMapper.toDisplayName(
                            relation.relationCode()
                    ))
                    .orElse("-");
        }

        return new BlacklistSubject(
                SubjectType.SHAREHOLDER,
                Long.toString(kyc.id()),
                kyc.ocrCnp(),
                kyc.title()
        );
    }
}
