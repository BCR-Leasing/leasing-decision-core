package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistDecision;

public interface CheckBlacklistUseCase {
    BlacklistDecision check(BlacklistAskCommand command);
}
