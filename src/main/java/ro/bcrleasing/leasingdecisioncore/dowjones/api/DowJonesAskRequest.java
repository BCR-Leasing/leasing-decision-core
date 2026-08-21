package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubjectType;

@Schema(
        name = "DowJonesAskRequest",
        description = """
                Request for executing a Dow Jones screening for a subject
                associated with an eBCRL lease.
                """
)
public record DowJonesAskRequest(

        @NotNull
        @Schema(
                description = "Caller-generated correlation identifier",
                example = "574f9595-5e19-4a46-9f5d-27ba22e695a2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID requestId,

        @NotNull
        @Positive
        @Schema(
                description = "Internal identifier from leases.id",
                example = "123",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long leaseId,

        @NotNull
        @Schema(
                description = "Subject type. The first increment supports COMPANY.",
                example = "COMPANY",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        DowJonesSubjectType subjectType,

        @NotBlank
        @Schema(
                description = """
                        Internal subject identifier. For COMPANY this is companies.id,
                        not the company CUI.
                        """,
                example = "456",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String subjectId
) {
}
