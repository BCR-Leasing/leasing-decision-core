package ro.bcrleasing.leasingdecisioncore.dowjones.api;

import org.springframework.stereotype.Component;

import ro.bcrleasing.leasingdecisioncore.dowjones.application.DowJonesAskCommand;
import ro.bcrleasing.leasingdecisioncore.dowjones.application.DowJonesAskEnvelope;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesProcessingStatus;

@Component
public class DowJonesApiMapper {

    public DowJonesAskCommand toCommand(DowJonesAskRequest request) {
        return new DowJonesAskCommand(
                request.requestId(),
                request.leaseId(),
                request.subjectType(),
                request.subjectId()
        );
    }

    public DowJonesAskResponse toResponse(DowJonesAskEnvelope envelope) {
        var result = envelope.result();
        var subject = result.subject();

        return new DowJonesAskResponse(
                envelope.context().requestId(),
                envelope.context().sessionId(),
                envelope.context().askId(),
                DowJonesProcessingStatus.COMPLETED,
                result.screeningStatus(),
                result.resultsFound(),
                new DowJonesSubjectResponse(
                        subject.type(),
                        subject.sourceId(),
                        subject.displayName()
                ),
                result.matches().stream()
                        .map(match -> new DowJonesMatchResponse(
                                match.profileId(),
                                match.name(),
                                match.gender(),
                                match.dateOfBirth(),
                                match.country(),
                                match.details(),
                                match.subsidiary(),
                                match.score()
                        ))
                        .toList(),
                toDocumentResponse(envelope),
                envelope.completedAt()
        );
    }

    private DowJonesDocumentResponse toDocumentResponse(DowJonesAskEnvelope envelope) {
        var result = envelope.result();
        var document = result.document();

        if (document == null) {
            return new DowJonesDocumentResponse(
                    false,
                    null,
                    null,
                    0L,
                    null,
                    null,
                    null,
                    null
            );
        }

        String downloadUrl = "/api/v1/dow-jones/asks/"
                + envelope.context().askId()
                + "/document";

        return new DowJonesDocumentResponse(
                true,
                document.fileName(),
                document.contentType(),
                document.size(),
                document.sha256(),
                downloadUrl,
                document.expiresAt(),
                result.documentPath()
        );
    }
}
