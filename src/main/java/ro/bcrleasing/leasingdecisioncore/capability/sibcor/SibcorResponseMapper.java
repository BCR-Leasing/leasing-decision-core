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

    public SibcorResponseMapper(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public SibcorFacts map(
            JsonNode root
    ) {
        return map(
                root,
                null
        );
    }

    public SibcorFacts map(
            JsonNode root,
            String cnpCui
    ) {
        if (root == null
                || root.isNull()
                || root.isMissingNode()
                || !root.isObject()) {

            throw new ExternalCapabilityException(
                    CAPABILITY,
                    "SIBCOR returned an empty or invalid response."
            );
        }

        List<SibcorBlacklistItem> blacklistItems =
                mapBlacklistItems(
                        root,
                        cnpCui
                );

        return new SibcorFacts(
                mapBlackListed(root),
                blacklistItems,
                mapRisk(root),
                mapNorkom(root),
                toEnrichedResponseMap(
                        root,
                        blacklistItems
                )
        );
    }

    private boolean mapBlackListed(
            JsonNode root
    ) {
        Object value =
                toObjectOrNull(
                        root.path("blacklist")
                                .path("blackListed")
                );

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(
                    stringValue.trim()
            );
        }

        return false;
    }

    private List<SibcorBlacklistItem> mapBlacklistItems(
            JsonNode root,
            String cnpCui
    ) {
        JsonNode items =
                root.path("blacklist")
                        .path("blackList");

        if (!items.isArray()) {
            return List.of();
        }

        List<SibcorBlacklistItem> result =
                new ArrayList<>();

        for (JsonNode item : items) {
            if (!item.isObject()) {
                throw new ExternalCapabilityException(
                        CAPABILITY,
                        "SIBCOR returned an invalid blacklist item."
                );
            }

            Map<String, Object> details =
                    toBlacklistItemDetails(
                            item,
                            cnpCui
                    );

            result.add(
                    new SibcorBlacklistItem(
                            toObjectOrNull(
                                    item.get("foundFlag")
                            ),
                            details
                    )
            );
        }

        return List.copyOf(result);
    }

    private Map<String, Object> toBlacklistItemDetails(
            JsonNode item,
            String cnpCui
    ) {
        LinkedHashMap<String, Object> original =
                toMutableObjectMap(item);

        LinkedHashMap<String, Object> enriched =
                new LinkedHashMap<>();

        boolean cnpCuiInserted = false;

        for (Map.Entry<String, Object> entry
                : original.entrySet()) {

            String key = entry.getKey();

            if ("cnpCui".equals(key)) {
                continue;
            }

            if ("nameInfo".equals(key)
                    && hasText(cnpCui)) {

                enriched.put(
                        "cnpCui",
                        cnpCui
                );

                cnpCuiInserted = true;
            }

            enriched.put(
                    key,
                    entry.getValue()
            );
        }

        if (!cnpCuiInserted
                && hasText(cnpCui)) {

            enriched.put(
                    "cnpCui",
                    cnpCui
            );
        }

        return Collections.unmodifiableMap(enriched);
    }

    private Map<String, Object> toEnrichedResponseMap(
            JsonNode root,
            List<SibcorBlacklistItem> blacklistItems
    ) {
        LinkedHashMap<String, Object> response =
                toMutableObjectMap(root);

        Object blacklistValue =
                response.get("blacklist");

        if (blacklistValue instanceof Map<?, ?> blacklistSource) {
            LinkedHashMap<String, Object> blacklist =
                    copyStringKeyMap(blacklistSource);

            List<Map<String, Object>> enrichedItems =
                    blacklistItems
                            .stream()
                            .map(SibcorBlacklistItem::details)
                            .toList();

            blacklist.put(
                    "blackList",
                    enrichedItems
            );

            response.put(
                    "blacklist",
                    Collections.unmodifiableMap(blacklist)
            );
        }

        return Collections.unmodifiableMap(response);
    }

    private SibcorRiskData mapRisk(
            JsonNode root
    ) {
        JsonNode risk = root.get("risk");

        if (risk == null
                || risk.isNull()
                || !risk.isObject()) {

            return null;
        }

        return new SibcorRiskData(
                toObjectOrNull(
                        risk.get("foundFlag")
                ),
                toDetailsMap(
                        risk.get("risk")
                )
        );
    }

    private SibcorNorkomData mapNorkom(
            JsonNode root
    ) {
        JsonNode norkom = root.get("norkom");

        if (norkom == null
                || norkom.isNull()
                || !norkom.isObject()) {

            return null;
        }

        return new SibcorNorkomData(
                toObjectOrNull(
                        norkom.get("score")
                ),
                textOrNull(
                        norkom.get("text")
                ),
                toObjectOrNull(
                        norkom.get("userAction")
                ),
                toDetailsMap(norkom)
        );
    }

    private Object toObjectOrNull(
            JsonNode node
    ) {
        if (node == null
                || node.isNull()
                || node.isMissingNode()) {

            return null;
        }

        return objectMapper.convertValue(
                node,
                Object.class
        );
    }

    private Map<String, Object> toDetailsMap(
            JsonNode node
    ) {
        if (node == null
                || node.isNull()
                || node.isMissingNode()) {

            return Map.of();
        }

        if (node.isObject()) {
            return Collections.unmodifiableMap(
                    toMutableObjectMap(node)
            );
        }

        LinkedHashMap<String, Object> wrapped =
                new LinkedHashMap<>();

        wrapped.put(
                "value",
                objectMapper.convertValue(
                        node,
                        Object.class
                )
        );

        return Collections.unmodifiableMap(wrapped);
    }

    private LinkedHashMap<String, Object> toMutableObjectMap(
            JsonNode node
    ) {
        return objectMapper.convertValue(
                node,
                new TypeReference<
                        LinkedHashMap<String, Object>
                >() {
                }
        );
    }

    private LinkedHashMap<String, Object> copyStringKeyMap(
            Map<?, ?> source
    ) {
        LinkedHashMap<String, Object> copy =
                new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            copy.put(
                    String.valueOf(entry.getKey()),
                    entry.getValue()
            );
        }

        return copy;
    }

    private String textOrNull(
            JsonNode node
    ) {
        if (node == null
                || node.isNull()
                || node.isMissingNode()) {

            return null;
        }

        return node.isTextual()
                ? node.textValue()
                : node.asText();
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
