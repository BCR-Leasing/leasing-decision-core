package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record SibcorRiskData(Object foundFlag, Map<String, Object> details) {

    public SibcorRiskData {
        details = immutableMap(details);
    }

    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}