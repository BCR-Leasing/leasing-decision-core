package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import org.springframework.stereotype.Component;
import ro.bcrleasing.leasingdecisioncore.ask.application.BlacklistDecisionEnvelope;
import ro.bcrleasing.leasingdecisioncore.blacklist.application.BlacklistAskCommand;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.ProcessingStatus;

@Component
public class BlacklistApiMapper {
    public BlacklistAskCommand toCommand(BlacklistAskRequest request) {
        return new BlacklistAskCommand(request.requestId(), request.leaseId(), request.subjectType(), request.subjectId());
    }

    public BlacklistDecisionResponse toResponse(BlacklistDecisionEnvelope envelope) {
        var decision = envelope.decision();
        var subject = decision.subject();
        return new BlacklistDecisionResponse(
                envelope.context().requestId(),
                envelope.context().sessionId(),
                envelope.context().askId(),
                ProcessingStatus.COMPLETED,
                decision.verdict(),
                new BlacklistSubjectResponse(
                        subject.type(),
                        subject.sourceId(),
                        subject.name(),
                        subject.role()),
                        decision.reasonCodes(),
                        decision.findings()
                                .stream()
                                .map(finding -> new DecisionFindingResponse(
                                        finding.source(),
                                        finding.category(),
                                        finding.details()))
                                .toList(),
                        decision.ruleVersion(),
                        envelope.completedAt());
    }
}
