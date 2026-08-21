package ro.bcrleasing.leasingdecisioncore.dowjones.application;

import ro.bcrleasing.leasingdecisioncore.dowjones.domain.DowJonesResult;

import java.util.UUID;

public interface CheckDowJonesUseCase {

    DowJonesResult check(
            DowJonesAskCommand command,
            UUID askId
    );
}
