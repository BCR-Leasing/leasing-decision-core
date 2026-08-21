package ro.bcrleasing.leasingdecisioncore.dowjones.application;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.CompanyDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.CompanyData;
import ro.bcrleasing.leasingdecisioncore.common.exception.InvalidSubjectDataException;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesResult;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubject;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubjectType;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.StoredDowJonesDocument;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesDocumentStorePort;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesScreeningPort;

@Service
public class DowJonesOrchestrator
        implements CheckDowJonesUseCase {

    private final CompanyDataPort companyDataPort;
    private final DowJonesScreeningPort screeningPort;
    private final DowJonesDocumentStorePort documentStorePort;

    public DowJonesOrchestrator(
            CompanyDataPort companyDataPort,
            DowJonesScreeningPort screeningPort,
            DowJonesDocumentStorePort documentStorePort
    ) {
        this.companyDataPort = Objects.requireNonNull(
                companyDataPort,
                "companyDataPort is required"
        );

        this.screeningPort = Objects.requireNonNull(
                screeningPort,
                "screeningPort is required"
        );

        this.documentStorePort = Objects.requireNonNull(
                documentStorePort,
                "documentStorePort is required"
        );
    }

    @Override
    public DowJonesResult check(
            DowJonesAskCommand command,
            UUID askId
    ) {
        Objects.requireNonNull(
                command,
                "command is required"
        );

        Objects.requireNonNull(
                askId,
                "askId is required"
        );

        if (command.subjectType()
                != DowJonesSubjectType.COMPANY) {

            throw new InvalidSubjectDataException(
                    "Only COMPANY is supported "
                            + "for the first Dow Jones increment."
            );
        }

        long companyId = parseCompanyId(
                command.subjectId()
        );

        CompanyData company = companyDataPort
                .findByLeaseIdAndCompanyId(
                        command.leaseId(),
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company does not exist "
                                        + "or is not assigned "
                                        + "to the lease."
                        )
                );

        if (!StringUtils.hasText(company.name())) {
            throw new InvalidSubjectDataException(
                    "The company name is missing."
            );
        }

        DowJonesSubject subject =
                new DowJonesSubject(
                        DowJonesSubjectType.COMPANY,
                        Long.toString(company.id()),
                        company.name().trim()
                );

        DowJonesScreeningFacts screeningFacts =
                screeningPort.screen(subject);

        StoredDowJonesDocument storedDocument =
                documentStorePort.store(
                        askId,
                        screeningFacts.document()
                );

        return new DowJonesResult(
                subject,
                screeningFacts.screeningStatus(),
                screeningFacts.resultsFound(),
                screeningFacts.matches(),
                storedDocument
        );
    }

    private long parseCompanyId(
            String subjectId
    ) {
        if (!StringUtils.hasText(subjectId)) {
            throw new InvalidSubjectDataException(
                    "subjectId is required."
            );
        }

        try {
            return Long.parseLong(
                    subjectId.trim()
            );
        } catch (NumberFormatException exception) {
            throw new InvalidSubjectDataException(
                    "For COMPANY, subjectId "
                            + "must be companies.id.",
                    exception
            );
        }
    }
}
