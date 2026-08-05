package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.CompanyDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.CompanyData;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;

@Component
public class CompanySubjectProvider implements BlacklistSubjectProvider {

    private final CompanyDataPort companyDataPort;

    public CompanySubjectProvider(CompanyDataPort companyDataPort) {
        this.companyDataPort = companyDataPort;
    }

    @Override
    public SubjectType supportedType() {
        return SubjectType.COMPANY;
    }

    @Override
    public BlacklistSubject loadSubject(long leaseId, String subjectId) {
        long companyId = SubjectIdParser.parseLong(
                subjectId,
                SubjectType.COMPANY
        );

        CompanyData company = companyDataPort
                .findByLeaseIdAndCompanyId(leaseId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company does not exist or is not assigned to the lease."
                ));

        return new BlacklistSubject(
                SubjectType.COMPANY,
                Long.toString(company.id()),
                company.cui(),
                company.name(),
                null
        );
    }
}
