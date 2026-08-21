package ro.bcrleasing.leasingdecisioncore.dowjones.port.out;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesScreeningFacts;
import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesSubject;

public interface DowJonesScreeningPort {

    DowJonesScreeningFacts screen(
            DowJonesSubject subject
    );
}
