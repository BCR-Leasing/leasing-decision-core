package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record SibcorBlacklistItem(
        Object foundFlag,
        Map<String, Object> details
) {

    public SibcorBlacklistItem {
        details = immutableMap(
                details
        );
    }

    public boolean isFound() {
        if (foundFlag instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (foundFlag instanceof String stringValue) {
            return Boolean.parseBoolean(
                    stringValue.trim()
            );
        }

        return false;
    }

    private static Map<String, Object> immutableMap(
            Map<String, Object> source
    ) {
        if (source == null
                || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(
                        source
                )
        );
    }
}