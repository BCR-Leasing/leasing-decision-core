package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.DealerDataPort;
import ro.bcrleasing.leasingdecisioncore.blacklist.port.out.model.DealerData;
import ro.bcrleasing.leasingdecisioncore.common.exception.ResourceNotFoundException;

@Component
public class DealerSubjectProvider implements BlacklistSubjectProvider {

    private final DealerDataPort dealerDataPort;

    public DealerSubjectProvider(DealerDataPort dealerDataPort) {
        this.dealerDataPort = dealerDataPort;
    }

    @Override
    public SubjectType supportedType() {
        return SubjectType.DEALER;
    }

    @Override
    public BlacklistSubject loadSubject(long leaseId, String subjectId) {
        long dealerId = SubjectIdParser.parseLong(
                subjectId,
                SubjectType.DEALER
        );

        DealerData dealer = dealerDataPort
                .findByLeaseIdAndDealerId(leaseId, dealerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dealer does not exist or is not assigned to the lease."
                ));

        return new BlacklistSubject(
                SubjectType.DEALER,
                Long.toString(dealer.id()),
                dealer.cui(),
                dealer.name(),
                null
        );
    }
}
