package org.example.dao.impl;

import org.example.connection.ConnectionPool;
import org.example.dao.AccountDao;
import org.example.model.Account;
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

import static org.example.dao.ColumnName.BANK_ID;
import static org.example.dao.ColumnName.CLIENT_ID;
import static org.example.dao.ColumnName.CURRENCY;
import static org.example.dao.ColumnName.ID;
import static org.example.dao.ColumnName.REST;
import static org.example.util.DaoUtils.validateAffectedRows;
import static org.example.util.DaoUtils.validateGeneratedKeys;

public class AccountDaoImpl implements AccountDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDaoImpl.class);
    static final String CREATE_ACCOUNT_QUERY = "INSERT INTO accounts (bank_id, client_id, currency, rest) VALUES (?, ?, ?, ?)";
    private static final String GET_ACCOUNT_QUERY = "SELECT id, bank_id, client_id, currency, rest FROM accounts WHERE id = ?";
    private static final String GET_CLIENT_ACCOUNTS = "SELECT id, bank_id, client_id, currency, rest FROM accounts WHERE client_id = ?";
    private static final String IS_ACCOUNT_EXISTS_QUERY = "SELECT 1 FROM accounts WHERE id = ?";
    private static final String IS_ENOUGH_MONEY_IN_ACCOUNT_QUERY = "SELECT 1 FROM accounts WHERE id = ? AND rest >= ?";
    private static final String GET_ALL_ACCOUNTS = "SELECT id, bank_id, client_id, currency, rest FROM accounts";
    private static final AccountDaoImpl INSTANCE = new AccountDaoImpl();
    private final ConnectionPool pool = ConnectionPool.getInstance();
    public static AccountDaoImpl getInstance() {
        return INSTANCE;
    }
    private AccountDaoImpl(){}

    @Override
    public Account create(final Account account) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_ACCOUNT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, account.getBankId());
            statement.setInt(2, account.getClientId());
            statement.setString(3, account.getCurrency());
            statement.setInt(4, account.getRest());
            int affectedRows = statement.executeUpdate();
            validateAffectedRows(affectedRows);

            final ResultSet generatedKeys = statement.getGeneratedKeys();
            validateGeneratedKeys(generatedKeys);

            account.setId(generatedKeys.getInt(1));
            return account;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public Optional<Account> getById(final int id) {
        try (Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_ACCOUNT_QUERY)) {
            statement.setInt(1, id);
            final ResultSet resultSet = statement.executeQuery();
            return Optional.ofNullable(resultSet.next() ? createAccountFromResultSet(resultSet) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public List<Account> getClientAccounts(final int clientId) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_CLIENT_ACCOUNTS)) {
            statement.setInt(1, clientId);
            final ResultSet resultSet = statement.executeQuery();
            final List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                accounts.add(createAccountFromResultSet(resultSet));
            }
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public List<Account> getAll() {
        try(Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_ALL_ACCOUNTS)) {
            final ResultSet resultSet = statement.executeQuery();
            final List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                accounts.add(createAccountFromResultSet(resultSet));
            }
            return accounts;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public boolean isAccountExists(final int id) {
        try(Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(IS_ACCOUNT_EXISTS_QUERY)) {
            statement.setInt(1, id);
            final ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getBoolean(1);
        }catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public boolean isEnoughMoneyInAccount(final int id, final int requestedAmount) {
        try(Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(IS_ENOUGH_MONEY_IN_ACCOUNT_QUERY)) {
            statement.setInt(1, id);
            statement.setInt(2, requestedAmount);
            final ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    private Account createAccountFromResultSet(final ResultSet resultSet) throws SQLException {
        final Account account = new Account();
        account.setId(resultSet.getInt(ID));
        account.setBankId(resultSet.getInt(BANK_ID));
        account.setClientId(resultSet.getInt(CLIENT_ID));
        account.setCurrency(resultSet.getString(CURRENCY));
        account.setRest(resultSet.getInt(REST));
        return account;
    }
}
