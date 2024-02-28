package org.example.dao;

import org.example.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientDao {
    Client create(final Client client);
    Optional<Client> getById(final int id);
    List<Client> getALl();
    Client update(final Client client);
    void deleteById(final int id);
}
