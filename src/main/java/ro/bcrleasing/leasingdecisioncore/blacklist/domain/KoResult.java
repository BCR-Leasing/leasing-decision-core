package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Objects;

public enum KoResult {

    OK,
    NOK;

    public static KoResult from(
            Verdict verdict
    ) {
        Objects.requireNonNull(
                verdict,
                "verdict is required"
        );

        return verdict == Verdict.PASSED
                ? OK
                : NOK;
    }
}