package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

public interface BlacklistDecisionEvaluator {

    BlacklistDecision evaluate(PreparedBlacklistInput input);
}
