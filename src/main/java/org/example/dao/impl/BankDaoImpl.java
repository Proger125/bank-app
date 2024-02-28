package org.example.dao.impl;

import org.example.connection.ConnectionPool;
import org.example.dao.BankDao;
import org.example.model.Bank;
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

import static org.example.dao.ColumnName.ID;
import static org.example.dao.ColumnName.INDIVIDUAL_COMMISSION;
import static org.example.dao.ColumnName.LEGAL_COMMISSION;
import static org.example.dao.ColumnName.NAME;
import static org.example.util.DaoUtils.validateAffectedRows;
import static org.example.util.DaoUtils.validateGeneratedKeys;

public class BankDaoImpl implements BankDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankDaoImpl.class);
    private static final String CREATE_BANK_QUERY = "INSERT INTO banks (name, individual_commission, legal_commission) VALUES (?, ?, ?)";
    private static final String GET_BANK_QUERY = "SELECT id, name, individual_commission, legal_commission FROM banks WHERE id = ?";
    private static final String UPDATE_BANK_QUERY = "UPDATE banks SET name = ?, individual_commission = ?, legal_commission = ? WHERE id = ?";
    private static final String REMOVE_BANK_QUERY = "DELETE FROM banks WHERE id = ?";
    private static final String GET_ALL_BANKS_QUERY = "SELECT id, name, individual_commission, legal_commission FROM banks";
    private static final BankDaoImpl INSTANCE = new BankDaoImpl();
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public static BankDaoImpl getInstance() {
        return INSTANCE;
    }
    private BankDaoImpl(){}
    @Override
    public Bank create(final Bank bank) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_BANK_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, bank.getName());
            statement.setInt(2, bank.getIndividualCommission());
            statement.setInt(3, bank.getLegalCommission());
            int affectedRows = statement.executeUpdate();
            validateAffectedRows(affectedRows);

            final ResultSet generatedKeys = statement.getGeneratedKeys();
            validateGeneratedKeys(generatedKeys);

            bank.setId(generatedKeys.getInt(1));
            return bank;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public Optional<Bank> getById(final int id) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BANK_QUERY)) {
            statement.setInt(1, id);
            final ResultSet resultSet = statement.executeQuery();
            return Optional.ofNullable(resultSet.next() ? createBankFromResultSet(resultSet) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public List<Bank> getAll() {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_BANKS_QUERY)) {
            final ResultSet resultSet = statement.executeQuery();
            final List<Bank> banks = new ArrayList<>();
            while (resultSet.next()) {
                banks.add(createBankFromResultSet(resultSet));
            }
            return banks;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public Bank update(final Bank bank) {
        Connection connection = null;
        try{
            connection = pool.getConnection();
            connection.setAutoCommit(false);
            final PreparedStatement updateStatement = connection.prepareStatement(UPDATE_BANK_QUERY);
            updateStatement.setString(1, bank.getName());
            updateStatement.setInt(2, bank.getIndividualCommission());
            updateStatement.setInt(3, bank.getLegalCommission());
            updateStatement.setInt(4, bank.getId());
            int affectedRows = updateStatement.executeUpdate();
            validateAffectedRows(affectedRows);

            final PreparedStatement getStatement = connection.prepareStatement(GET_BANK_QUERY);
            getStatement.setInt(1, bank.getId());
            final ResultSet resultSet = getStatement.executeQuery();
            if (resultSet.next()) {
                return createBankFromResultSet(resultSet);
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
             PreparedStatement statement = connection.prepareStatement(REMOVE_BANK_QUERY)) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            validateAffectedRows(affectedRows);
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    private Bank createBankFromResultSet(final ResultSet resultSet) throws SQLException {
        final Bank bank = new Bank();
        bank.setId(resultSet.getInt(ID));
        bank.setName(resultSet.getString(NAME));
        bank.setIndividualCommission(resultSet.getInt(INDIVIDUAL_COMMISSION));
        bank.setLegalCommission(resultSet.getInt(LEGAL_COMMISSION));
        return bank;
    }
}
