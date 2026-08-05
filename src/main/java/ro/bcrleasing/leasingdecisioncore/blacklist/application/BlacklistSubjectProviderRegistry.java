package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;
import ro.bcrleasing.leasingdecisioncore.common.exception.UnsupportedSubjectTypeException;

@Component
public class BlacklistSubjectProviderRegistry {

    private final Map<SubjectType, BlacklistSubjectProvider> providers;

    public BlacklistSubjectProviderRegistry(
            List<BlacklistSubjectProvider> providerList
    ) {
        EnumMap<SubjectType, BlacklistSubjectProvider> registry =
                new EnumMap<>(SubjectType.class);

        for (BlacklistSubjectProvider provider : providerList) {
            BlacklistSubjectProvider previous =
                    registry.put(provider.supportedType(), provider);

            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate provider for subject type "
                                + provider.supportedType()
                );
            }
        }

        this.providers = Map.copyOf(registry);
    }

    public BlacklistSubjectProvider get(SubjectType subjectType) {
        BlacklistSubjectProvider provider = providers.get(subjectType);
        if (provider == null) {
            throw new UnsupportedSubjectTypeException(subjectType);
        }
        return provider;
    }
}
