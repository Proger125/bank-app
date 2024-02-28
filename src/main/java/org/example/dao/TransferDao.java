package org.example.dao;

import org.example.model.Transfer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TransferDao {
    Transfer create(final Transfer transfer);

    Optional<Transfer> getById(final int id);

    List<Transfer> getClientTransfersForPeriod(final int clientId, final Date startDate, final Date endDate);
}
