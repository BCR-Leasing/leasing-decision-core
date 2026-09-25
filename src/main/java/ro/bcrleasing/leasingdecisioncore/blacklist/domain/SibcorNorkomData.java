package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record SibcorNorkomData(Object score, String text, Object userAction, Map<String, Object> details) {

    public SibcorNorkomData {
        details = immutableMap(details);
    }

    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}