package ro.bcrleasing.leasingdecisioncore.blacklist.port.out;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.BlacklistSubject;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorFacts;

public interface SibcorPort {

    SibcorFacts checkBlacklist(BlacklistSubject subject);
}
