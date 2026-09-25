package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SibcorFacts(
        boolean blackListed,
        List<SibcorBlacklistItem> blacklistItems,
        SibcorRiskData risk,
        SibcorNorkomData norkom,
        Map<String, Object> response
) {

    public SibcorFacts {
        blacklistItems = blacklistItems == null
                ? List.of()
                : List.copyOf(blacklistItems);

        response = immutableMap(response);
    }

    public SibcorFacts(
            List<SibcorBlacklistItem> blacklistItems,
            SibcorRiskData risk,
            SibcorNorkomData norkom
    ) {
        this(
                false,
                blacklistItems,
                risk,
                norkom,
                Map.of()
        );
    }

    public static SibcorFacts empty() {
        return new SibcorFacts(
                false,
                List.of(),
                null,
                null,
                Map.of()
        );
    }

    public boolean hasBlacklistMatch() {
        boolean blacklistMatch =
                blacklistItems
                        .stream()
                        .filter(Objects::nonNull)
                        .anyMatch(
                                SibcorBlacklistItem::isFound
                        );

        boolean norkomMatch =
                norkom != null
                        && norkom.userAction() != null
                        && "Blacklisted Customer".equalsIgnoreCase(
                        norkom.userAction()
                                .toString()
                                .trim()
                );

        return blacklistMatch || norkomMatch;
    }

    private static Map<String, Object> immutableMap(
            Map<String, Object> source
    ) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(source)
        );
    }
}
