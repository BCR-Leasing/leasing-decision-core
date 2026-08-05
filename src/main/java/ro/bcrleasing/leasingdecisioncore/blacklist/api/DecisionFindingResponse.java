package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.FindingCategory;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.FindingSource;

public record DecisionFindingResponse(FindingSource source, FindingCategory category, Map<String, Object> details) {
    public DecisionFindingResponse {
        if (details == null || details.isEmpty()) {
            details = Map.of();
        } else {
            details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
        }
    }
}
