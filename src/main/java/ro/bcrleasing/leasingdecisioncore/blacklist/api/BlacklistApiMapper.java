package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.ask.application.BlacklistDecisionEnvelope;
import ro.bcrleasing.leasingdecisioncore.blacklist.application.BlacklistAskCommand;
import ro.bcrleasing.leasingdecisioncore.blacklist.application.BlacklistQuery;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistBatchItem;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecision;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistItemStatus;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.KoResult;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.ProcessingStatus;

@Component
public class BlacklistApiMapper {

    public BlacklistAskCommand toCommand(
            BlacklistAskRequest request
    ) {
        List<BlacklistQuery> queries =
                request.effectiveQueries()
                        .stream()
                        .map(query ->
                                new BlacklistQuery(
                                        query.cnpCui(),
                                        query.clientName()
                                )
                        )
                        .toList();

        return new BlacklistAskCommand(
                UUID.randomUUID(),
                queries
        );
    }

    public BlacklistDecisionResponse toResponse(
            BlacklistDecisionEnvelope envelope
    ) {
        return new BlacklistDecisionResponse(
                envelope.context().requestId(),
                envelope.context().sessionId(),
                envelope.context().askId(),
                ProcessingStatus.COMPLETED,
                envelope.decision()
                        .items()
                        .stream()
                        .map(this::toItemResponse)
                        .toList(),
                envelope.completedAt()
        );
    }

    private BlacklistDecisionItemResponse toItemResponse(
            BlacklistBatchItem item
    ) {
        if (item.status() == BlacklistItemStatus.NOT_FOUND) {
            return new BlacklistDecisionItemResponse(
                    item.cnpCui(),
                    item.clientName(),
                    item.status(),
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    item.errorCode(),
                    item.errorMessage()
            );
        }

        BlacklistDecision decision = item.decision();
        var subject = decision.subject();

        return new BlacklistDecisionItemResponse(
                item.cnpCui(),
                item.clientName(),
                item.status(),
                decision.verdict(),
                KoResult.from(decision.verdict()),
                new BlacklistSubjectResponse(
                        subject.type(),
                        subject.sourceId(),
                        subject.name()
                ),
                decision.reasonCodes(),
                decision.findings()
                        .stream()
                        .map(finding ->
                                new DecisionFindingResponse(
                                        finding.source(),
                                        finding.category(),
                                        finding.details()
                                )
                        )
                        .toList(),
                null,
                null
        );
    }
}
