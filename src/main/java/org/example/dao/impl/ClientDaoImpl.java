package org.example.dao.impl;

import org.example.connection.ConnectionPool;
import org.example.dao.ClientDao;
import org.example.model.Account;
import org.example.model.Client;
import org.example.model.ClientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.dao.ColumnName.CLIENT_TYPE;
import static org.example.dao.ColumnName.CLIENT_TYPE_ID;
import static org.example.dao.ColumnName.ID;
import static org.example.dao.ColumnName.NAME;
import static org.example.dao.impl.AccountDaoImpl.CREATE_ACCOUNT_QUERY;
import static org.example.util.DaoUtils.validateAffectedRows;
import static org.example.util.DaoUtils.validateGeneratedKeys;

public class ClientDaoImpl implements ClientDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDaoImpl.class);
    private static final int DEFAULT_ACCOUNT_INDEX = 0;
    private static final int INDIVIDUAL_TYPE_ID = 1;
    private static final String GET_CLIENT_TYPE_ID_STATEMENT = "SELECT id FROM client_types WHERE type = ?";
    private static final String GET_CLIENT_TYPE_BY_ID_STATEMENT = "SELECT type FROM client_types WHERE id = ?";
    private static final String CREATE_CLIENT_QUERY = "INSERT INTO clients (name, client_type_id) VALUES (?, ?)";
    private static final String GET_CLIENT_QUERY = "SELECT id, name, client_type_id FROM clients WHERE id = ?";
    private static final String UPDATE_CLIENT_QUERY = "UPDATE clients SET name = ?, client_type_id = ? WHERE id = ?";
    private static final String DELETE_CLIENT_QUERY = "DELETE FROM clients WHERE id = ?";
    private static final String GET_ALL_CLIENTS_QUERY = "SELECT id, name, client_type_id FROM clients";
    private static final ClientDaoImpl INSTANCE = new ClientDaoImpl();
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public static ClientDaoImpl getInstance() {
        return INSTANCE;
    }

    private ClientDaoImpl(){}

    @Override
    public Client create(final Client client) {
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);
            final int clientTypeId = getClientTypeId(connection, client.getClientType());

            final PreparedStatement createClientStatement = connection.prepareStatement(CREATE_CLIENT_QUERY, Statement.RETURN_GENERATED_KEYS);
            createClientStatement.setString(1, client.getName());
            createClientStatement.setInt(2, clientTypeId);
            int affectedRows = createClientStatement.executeUpdate();
            validateAffectedRows(affectedRows);

            ResultSet generatedKeys = createClientStatement.getGeneratedKeys();
            validateGeneratedKeys(generatedKeys);
            client.setId(generatedKeys.getInt(1));

            final PreparedStatement createAccountStatement = connection.prepareStatement(CREATE_ACCOUNT_QUERY, Statement.RETURN_GENERATED_KEYS);
            final Account account = client.getAccounts().get(DEFAULT_ACCOUNT_INDEX);
            createAccountStatement.setInt(1, account.getBankId());
            createAccountStatement.setInt(2, client.getId());
            createAccountStatement.setString(3, account.getCurrency());
            createAccountStatement.setInt(4, account.getRest());
            affectedRows = createAccountStatement.executeUpdate();
            validateAffectedRows(affectedRows);

            generatedKeys = createAccountStatement.getGeneratedKeys();
            validateGeneratedKeys(generatedKeys);
            account.setId(generatedKeys.getInt(1));
            return client;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException exception) {
                    throw new RuntimeException("Something went wrong...", e);
                }
            }
            throw new RuntimeException("Something went wrong...", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Something went wrong...", e);
                }
            }
        }
    }

    @Override
    public Optional<Client> getById(final int id) {
        Connection connection = null;
        try {
            connection = pool.getConnection();
            final PreparedStatement statement = connection.prepareStatement(GET_CLIENT_QUERY);
            statement.setInt(1, id);
            final ResultSet resultSet = statement.executeQuery();
            return Optional.ofNullable(resultSet.next() ? createClientFromResultSet(resultSet, connection) : null);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException exception) {
                    throw new RuntimeException("Something went wrong...", e);
                }
            }
            throw new RuntimeException("Something went wrong...", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Something went wrong...", e);
                }
            }
        }
    }

    @Override
    public List<Client> getALl() {
        try(Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_ALL_CLIENTS_QUERY)) {
            final ResultSet resultSet = statement.executeQuery();
            final List<Client> clients = new ArrayList<>();
            while (resultSet.next()) {
                clients.add(createClientFromResultSet(resultSet, connection));
            }
            return clients;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public Client update(final Client client) {
        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);
            final PreparedStatement updateStatement = connection.prepareStatement(UPDATE_CLIENT_QUERY);
            updateStatement.setString(1, client.getName());
            updateStatement.setInt(2, getClientTypeId(connection, client.getClientType()));
            updateStatement.setInt(3, client.getId());
            int affectedRows = updateStatement.executeUpdate();
            validateAffectedRows(affectedRows);

            final PreparedStatement getStatement = connection.prepareStatement(GET_CLIENT_QUERY);
            getStatement.setInt(1, client.getId());
            final ResultSet resultSet = getStatement.executeQuery();
            if (resultSet.next()) {
                return createClientFromResultSet(resultSet, connection);
            }
            return null;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException exception) {
                    throw new RuntimeException("Something went wrong...", e);
                }
            }
            throw new RuntimeException("Something went wrong...", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Something went wrong...", e);
                }
            }
        }
    }

    @Override
    public void deleteById(final int id) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_CLIENT_QUERY)) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            validateAffectedRows(affectedRows);
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    private int getClientTypeId(final Connection connection, final ClientType clientType) throws SQLException {
        final PreparedStatement getClientTypeIdStatement = connection.prepareStatement(GET_CLIENT_TYPE_ID_STATEMENT);
        getClientTypeIdStatement.setString(1, clientType.name());
        final ResultSet clientTypeIdResultSet = getClientTypeIdStatement.executeQuery();
        return clientTypeIdResultSet.next() ? clientTypeIdResultSet.getInt(ID) : INDIVIDUAL_TYPE_ID;
    }

    private Client createClientFromResultSet(final ResultSet resultSet, final Connection connection) throws SQLException {
        final Client client = new Client();
        client.setId(resultSet.getInt(ID));
        client.setName(resultSet.getString(NAME));
        final PreparedStatement getClientTypeStatement = connection.prepareStatement(GET_CLIENT_TYPE_BY_ID_STATEMENT);
        getClientTypeStatement.setInt(1, resultSet.getInt(CLIENT_TYPE_ID));
        final ResultSet clientTypeResultSet = getClientTypeStatement.executeQuery();
        client.setClientType(clientTypeResultSet.next() ? ClientType.valueOf(clientTypeResultSet.getString(CLIENT_TYPE)) : ClientType.INDIVIDUAL);
        return client;
    }
}
