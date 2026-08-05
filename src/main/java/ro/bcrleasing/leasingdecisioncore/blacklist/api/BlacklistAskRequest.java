package ro.bcrleasing.leasingdecisioncore.blacklist.api;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ro.bcrleasing.leasingdecisioncore.blacklist.domain.SubjectType;

public record BlacklistAskRequest(
        @NotNull UUID requestId,
        @Positive long leaseId,
        @NotNull SubjectType subjectType,
        @NotBlank String subjectId
) {
}
