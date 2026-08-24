package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Service;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.*;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.NegativeInformationPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.SibcorPort;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubject;
import ro.bcrleasing.leasingdecisioncore.dowjones.port.out.DowJonesScreeningPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlacklistOrchestrator implements CheckBlacklistUseCase {

    private final BlacklistSubjectProviderRegistry subjectProviderRegistry;
    private final SibcorPort sibcorPort;
    private final NegativeInformationPort negativeInformationPort;
    private final DowJonesScreeningPort dowJonesScreeningPort;
    private final BlacklistDecisionEvaluator decisionEvaluator;

    public BlacklistOrchestrator(
            BlacklistSubjectProviderRegistry subjectProviderRegistry,
            SibcorPort sibcorPort,
            NegativeInformationPort negativeInformationPort,
            BlacklistDecisionEvaluator decisionEvaluator,
            DowJonesScreeningPort dowJonesScreeningPort
    ) {
        this.subjectProviderRegistry = subjectProviderRegistry;
        this.sibcorPort = sibcorPort;
        this.negativeInformationPort = negativeInformationPort;
        this.decisionEvaluator = decisionEvaluator;
        this.dowJonesScreeningPort = dowJonesScreeningPort;
    }

    @Override
    public BlacklistDecision check(
            BlacklistAskCommand command
    ) {
        BlacklistSubjectProvider provider =
                subjectProviderRegistry.get(
                        command.subjectType()
                );

        BlacklistSubject subject =
                provider.loadSubject(
                        command.leaseId(),
                        command.subjectId()
                );

        if (subject.identifier() == null
                || subject.identifier().isBlank()) {

            return decisionEvaluator.evaluate(
                    new PreparedBlacklistInput(
                            subject,
                            SibcorFacts.empty(),
                            InternalNegativeInformationFacts.notChecked()
                    )
            );
        }

        SibcorFacts sibcorFacts =
                sibcorPort.checkBlacklist(
                        subject
                );

        InternalNegativeInformationFacts internalFacts =
                negativeInformationPort.findByIdentifier(
                        subject.identifier()
                );

        DowJonesScreeningFacts dowJonesFacts = null;

        if (!requiresDowJonesScreening(
                sibcorFacts
        )) {
            DowJonesSubject dowJonesSubject =
                    new DowJonesSubject(
                            subject.type(),
                            subject.sourceId(),
                            subject.name()
                    );

            dowJonesFacts =
                    dowJonesScreeningPort.screen(
                            dowJonesSubject
                    );
        }

        BlacklistDecision decision =
                decisionEvaluator.evaluate(
                        new PreparedBlacklistInput(
                                subject,
                                sibcorFacts,
                                internalFacts
                        )
                );

        if (dowJonesFacts == null) {
            return decision;
        }

        return new BlacklistDecision(
                decision.subject(),
                decision.verdict(),
                decision.reasonCodes(),
                appendDowJonesFinding(
                        decision.findings(),
                        dowJonesFacts
                ),
                decision.ruleVersion()
        );
    }

    private boolean requiresDowJonesScreening(
            SibcorFacts sibcorFacts
    ) {
        if (sibcorFacts == null) {
            return false;
        }

        return sibcorFacts
                .blacklistItems()
                .stream()
                .map(
                        SibcorBlacklistItem::foundFlag
                )
                .anyMatch(
                        this::isTrueFlag
                );
    }

    private boolean isTrueFlag(
            Object foundFlag
    ) {
        if (Boolean.TRUE.equals(
                foundFlag
        )) {
            return true;
        }

        if (foundFlag instanceof String stringValue) {
            return Boolean.parseBoolean(
                    stringValue.trim()
            );
        }

        return false;
    }

    private DecisionFinding toDowJonesFinding(
            DowJonesScreeningFacts dowJonesFacts
    ) {
        Map<String, Object> details =
                new LinkedHashMap<>();

        Map<String, Object> document =
                new LinkedHashMap<>();

        details.put(
                "processingStatus",
                "COMPLETED"
        );

        details.put(
                "screeningStatus",
                dowJonesFacts
                        .screeningStatus()
                        .name()
        );

        details.put(
                "resultsFound",
                dowJonesFacts.resultsFound()
        );

        details.put(
                "matches",
                dowJonesFacts.matches()
        );

        details.put(
                "document",
                document
        );

        return new DecisionFinding(
                FindingSource.DOW_JONES,
                FindingCategory.SCREENING,
                details
        );
    }

    private List<DecisionFinding> appendDowJonesFinding(
            List<DecisionFinding> existingFindings,
            DowJonesScreeningFacts dowJonesFacts
    ) {
        List<DecisionFinding> findings =
                new ArrayList<>(
                        existingFindings == null
                                ? List.of()
                                : existingFindings
                );

        findings.add(
                toDowJonesFinding(
                        dowJonesFacts
                )
        );

        return List.copyOf(
                findings
        );
    }
}