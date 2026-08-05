package ro.bcrleasing.leasingdecisioncore.blacklist.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record InternalNegativeInformationFacts(
        boolean checked,
        boolean matched,
        Map<String, Object> details
) {
    public InternalNegativeInformationFacts {
        details = immutableMap(details);
    }

    public static InternalNegativeInformationFacts noMatch() {
        return new InternalNegativeInformationFacts(true, false, Map.of());
    }

    public static InternalNegativeInformationFacts match(Map<String, Object> details) {
        return new InternalNegativeInformationFacts(true, true, details);
    }

    public static InternalNegativeInformationFacts notChecked() {
        return new InternalNegativeInformationFacts(false, false, Map.of());
    }

    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
