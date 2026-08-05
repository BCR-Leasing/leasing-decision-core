package ro.bcrleasing.leasingdecisioncore.capability.sibcor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorBlacklistItem;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorFacts;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorNorkomData;
import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SibcorRiskData;
import ro.bcrleasing.leasingdecisioncore.common.exception.ExternalCapabilityException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SibcorResponseMapper {

    private static final String CAPABILITY = "SIBCOR";
    private final ObjectMapper objectMapper;

    public SibcorResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SibcorFacts map(JsonNode root) {
        if (root == null || root.isNull()) {
            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR returned an empty response."
            );
        }

        return new SibcorFacts(
                mapBlacklistItems(root),
                mapRisk(root),
                mapNorkom(root)
        );
    }

    private List<SibcorBlacklistItem> mapBlacklistItems(JsonNode root) {
        JsonNode items = root.path("blacklist").path("blackList");
        if (!items.isArray()) {
            return List.of();
        }

        List<SibcorBlacklistItem> result = new ArrayList<>();
        for (JsonNode item : items) {
            result.add(new SibcorBlacklistItem(
                    toObjectOrNull(item.get("foundFlag")),
                    toDetailsMap(item)
            ));
        }
        return result;
    }

    private SibcorRiskData mapRisk(JsonNode root) {
        JsonNode risk = root.get("risk");
        if (risk == null || !risk.isObject()) {
            return null;
        }

        return new SibcorRiskData(
                toObjectOrNull(risk.get("foundFlag")),
                toDetailsMap(risk.get("risk"))
        );
    }

    private SibcorNorkomData mapNorkom(JsonNode root) {
        JsonNode norkom = root.get("norkom");
        if (norkom == null || !norkom.isObject()) {
            return null;
        }

        return new SibcorNorkomData(
                toObjectOrNull(norkom.get("score")),
                textOrNull(norkom.get("text")),
                toObjectOrNull(norkom.get("userAction")),
                toDetailsMap(norkom)
        );
    }

    private Object toObjectOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return objectMapper.convertValue(node, Object.class);
    }

    private Map<String, Object> toDetailsMap(JsonNode node) {
        if (node == null || node.isNull()) {
            return Map.of();
        }

        if (node.isObject()) {
            LinkedHashMap<String, Object> converted =
                    objectMapper.convertValue(
                            node,
                            new TypeReference<LinkedHashMap<String, Object>>() {
                            }
                    );
            return Collections.unmodifiableMap(converted);
        }

        LinkedHashMap<String, Object> wrapped =
                new LinkedHashMap<>();
        wrapped.put(
                "value",
                objectMapper.convertValue(node, Object.class)
        );
        return Collections.unmodifiableMap(wrapped);
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isTextual() ? node.textValue() : node.asText();
    }
}
