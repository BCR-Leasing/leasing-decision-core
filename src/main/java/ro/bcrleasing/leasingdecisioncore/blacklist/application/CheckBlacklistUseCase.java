package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistBatchDecision;

public interface CheckBlacklistUseCase {

    BlacklistBatchDecision check(BlacklistAskCommand command);
}