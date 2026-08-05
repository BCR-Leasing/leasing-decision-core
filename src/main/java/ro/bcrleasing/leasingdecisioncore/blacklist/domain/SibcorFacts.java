package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.List;

public record SibcorFacts(
        List<SibcorBlacklistItem> blacklistItems,
        SibcorRiskData risk,
        SibcorNorkomData norkom
) {
    public SibcorFacts {
        blacklistItems = blacklistItems == null ? List.of() : List.copyOf(blacklistItems);
    }

    public static SibcorFacts empty() {
        return new SibcorFacts(List.of(), null, null);
    }
}
