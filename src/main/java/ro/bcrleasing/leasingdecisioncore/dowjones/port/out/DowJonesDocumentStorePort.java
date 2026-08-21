package ro.bcrleasing.leasingdecisioncore.dowjones.port.out;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesDocumentContent;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.GeneratedDowJonesDocument;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.StoredDowJonesDocument;

import java.util.Optional;
import java.util.UUID;

public interface DowJonesDocumentStorePort {

    StoredDowJonesDocument store(
            UUID askId,
            GeneratedDowJonesDocument document
    );

    Optional<DowJonesDocumentContent> find(
            UUID askId
    );
}
