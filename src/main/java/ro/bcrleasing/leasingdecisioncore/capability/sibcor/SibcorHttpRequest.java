package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import java.math.BigInteger;

public record SibcorHttpRequest(
        BigInteger cnpCui,
        String clientName
) {
}
