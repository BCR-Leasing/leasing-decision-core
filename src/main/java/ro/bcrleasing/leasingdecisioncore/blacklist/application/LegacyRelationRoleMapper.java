package ro.bcrleasing.leasingdecisioncore.blacklist.application;

import org.springframework.stereotype.Component;

@Component
public class LegacyRelationRoleMapper {

    public String toDisplayName(Integer relationCode) {
        if (relationCode == null) {
            return "-";
        }

        return switch (relationCode) {
            case 1 -> "Asociat";
            case 2 -> "Asociat unic";
            case 3 -> "Administrator";
            case 4 -> "Asociat in";
            case 5 -> "Cenzor";
            case 6 -> "Actionar";
            default -> "-";
        };
    }
}
