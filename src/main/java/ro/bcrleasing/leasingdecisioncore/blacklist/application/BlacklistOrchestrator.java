package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Service;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecision;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecisionEvaluator;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.InternalNegativeInformationFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.PreparedBlacklistInput;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.NegativeInformationPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.SibcorPort;

@Service
public class BlacklistOrchestrator implements CheckBlacklistUseCase {

    private final BlacklistSubjectProviderRegistry subjectProviderRegistry;
    private final SibcorPort sibcorPort;
    private final NegativeInformationPort negativeInformationPort;
    private final BlacklistDecisionEvaluator decisionEvaluator;

    public BlacklistOrchestrator(
            BlacklistSubjectProviderRegistry subjectProviderRegistry,
            SibcorPort sibcorPort,
            NegativeInformationPort negativeInformationPort,
            BlacklistDecisionEvaluator decisionEvaluator
    ) {
        this.subjectProviderRegistry = subjectProviderRegistry;
        this.sibcorPort = sibcorPort;
        this.negativeInformationPort = negativeInformationPort;
        this.decisionEvaluator = decisionEvaluator;
    }

    @Override
    public BlacklistDecision check(BlacklistAskCommand command) {
        BlacklistSubjectProvider provider = subjectProviderRegistry.get(command.subjectType());

        BlacklistSubject subject = provider.loadSubject(
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

        SibcorFacts sibcorFacts = sibcorPort.checkBlacklist(subject);

        InternalNegativeInformationFacts internalFacts = negativeInformationPort.findByIdentifier(subject.identifier());

        return decisionEvaluator.evaluate(
                new PreparedBlacklistInput(
                        subject,
                        sibcorFacts,
                        internalFacts
                )
        );
    }
}
