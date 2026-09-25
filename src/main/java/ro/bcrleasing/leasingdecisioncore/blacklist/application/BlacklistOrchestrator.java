package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistBatchDecision;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistBatchItem;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecision;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecisionEvaluator;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.InternalNegativeInformationFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.PreparedBlacklistInput;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.CompanyDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.RestrictedEntitiesPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.SibcorPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.CompanyData;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubject;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesScreeningPort;

@Service
public class BlacklistOrchestrator
        implements CheckBlacklistUseCase {

    private final CompanyDataPort companyDataPort;
    private final SibcorPort sibcorPort;
    private final RestrictedEntitiesPort restrictedEntitiesPort;
    private final DowJonesScreeningPort dowJonesScreeningPort;
    private final BlacklistDecisionEvaluator decisionEvaluator;

    public BlacklistOrchestrator(
            CompanyDataPort companyDataPort,
            SibcorPort sibcorPort,
            RestrictedEntitiesPort restrictedEntitiesPort,
            BlacklistDecisionEvaluator decisionEvaluator,
            DowJonesScreeningPort dowJonesScreeningPort
    ) {
        this.companyDataPort = companyDataPort;
        this.sibcorPort = sibcorPort;
        this.restrictedEntitiesPort = restrictedEntitiesPort;
        this.decisionEvaluator = decisionEvaluator;
        this.dowJonesScreeningPort = dowJonesScreeningPort;
    }

    @Override
    public BlacklistBatchDecision check(
            BlacklistAskCommand command
    ) {
        Objects.requireNonNull(
                command,
                "command is required"
        );

        List<BlacklistBatchItem> items =
                new ArrayList<>(
                        command.queries().size()
                );

        for (BlacklistQuery query : command.queries()) {
            Optional<ResolvedQuery> resolvedQuery =
                    resolveQuery(query);

            if (resolvedQuery.isEmpty()) {
                items.add(
                        BlacklistBatchItem
                                .clientNameNotResolved(
                                        query.cnpCui()
                                )
                );

                continue;
            }

            ResolvedQuery resolved =
                    resolvedQuery.orElseThrow();

            BlacklistDecision decision =
                    checkResolvedQuery(resolved);

            items.add(
                    BlacklistBatchItem.completed(
                            resolved.cnpCui(),
                            resolved.clientName(),
                            decision
                    )
            );
        }

        return new BlacklistBatchDecision(
                items
        );
    }

    private Optional<ResolvedQuery> resolveQuery(
            BlacklistQuery query
    ) {
        if (StringUtils.hasText(
                query.clientName()
        )) {
            return Optional.of(
                    new ResolvedQuery(
                            query.cnpCui(),
                            query.clientName().trim()
                    )
            );
        }

        return companyDataPort
                .findByCui(
                        query.cnpCui()
                )
                .map(CompanyData::name)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(clientName ->
                        new ResolvedQuery(
                                query.cnpCui(),
                                clientName
                        )
                );
    }

    private BlacklistDecision checkResolvedQuery(
            ResolvedQuery query
    ) {
        BlacklistSubject subject =
                new BlacklistSubject(
                        SubjectType.COMPANY,
                        query.cnpCui(),
                        query.cnpCui(),
                        query.clientName()
                );

        SibcorFacts sibcorFacts =
                sibcorPort.checkBlacklist(
                        subject
                );

        Optional<DowJonesScreeningFacts>
                dowJonesScreeningFacts =
                Optional.empty();

        if (sibcorFacts.hasBlacklistMatch()) {
            DowJonesSubject dowJonesSubject =
                    new DowJonesSubject(
                            SubjectType.COMPANY,
                            query.cnpCui(),
                            query.clientName()
                    );

            DowJonesScreeningFacts screeningFacts =
                    dowJonesScreeningPort.screen(
                            dowJonesSubject
                    );

            dowJonesScreeningFacts =
                    Optional.of(
                            screeningFacts
                    );
        }

        InternalNegativeInformationFacts internalFacts =
                restrictedEntitiesPort.findByIdentifier(
                        query.cnpCui()
                );

        return decisionEvaluator.evaluate(
                new PreparedBlacklistInput(
                        subject,
                        sibcorFacts,
                        internalFacts,
                        dowJonesScreeningFacts
                )
        );
    }

    private record ResolvedQuery(
            String cnpCui,
            String clientName
    ) {
    }
}
