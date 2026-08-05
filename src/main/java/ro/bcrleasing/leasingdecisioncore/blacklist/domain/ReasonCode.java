package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

public enum ReasonCode {
    IDENTIFIER_MISSING,
    SIBCOR_BLACKLIST_MATCH,
    SIBCOR_RISK_MATCH,
    SIBCOR_NORKOM_MATCH,
    INTERNAL_NEGATIVE_INFORMATION_MATCH
}
