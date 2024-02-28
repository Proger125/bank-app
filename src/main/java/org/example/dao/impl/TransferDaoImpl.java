package org.example.dao.impl;

import org.example.connection.ConnectionPool;
import org.example.dao.TransferDao;
import org.example.model.Account;
import org.example.model.ClientType;
import org.example.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.example.dao.ColumnName.AMOUNT;
import static org.example.dao.ColumnName.BANK_ID;
import static org.example.dao.ColumnName.CLIENT_ID;
import static org.example.dao.ColumnName.CURRENCY;
import static org.example.dao.ColumnName.DATE;
import static org.example.dao.ColumnName.ID;
import static org.example.dao.ColumnName.RECEIVER_ACCOUNT_ID;
import static org.example.dao.ColumnName.REST;
import static org.example.dao.ColumnName.SENDER_ACCOUNT_ID;
import static org.example.util.DaoUtils.validateAffectedRows;
import static org.example.util.DaoUtils.validateGeneratedKeys;

public class TransferDaoImpl implements TransferDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferDaoImpl.class);
    private static final String CREATE_TRANSFER_QUERY = "INSERT INTO transfers (sender_account_id, receiver_account_id, amount, date) VALUES (?, ?, ?, ?)";
    private static final String GET_TRANSFER_QUERY = "SELECT id, sender_account_id, receiver_account_id, amount, date FROM transfers WHERE id = ?";
    private static final String GET_CLIENT_TRANSFERS_FOR_PERIOD = "SELECT id, sender_account_id, receiver_account_id, amount, date FROM transfers WHERE "
            + "sender_account_id = ? AND date >= ? AND date <= ?";
    private static final String SEND_MONEY_QUERY = "UPDATE accounts SET rest = ? WHERE id = ?";
    private static final String RECEIVE_MONEY_QUERY = "UPDATE accounts SET rest = ? WHERE id = ?";
    private static final String GET_ACCOUNT_INFO_QUERY = "SELECT client_id, bank_id, currency, rest FROM accounts WHERE id = ?";
    private static final String GET_BANK_COMMISSION_FOR_INDIVIDUAL_QUERY = "SELECT individual_commission FROM banks WHERE id = ?";
    private static final String GET_BANK_COMMISSION_FOR_LEGAL_QUERY = "SELECT legal_commission FROM banks WHERE id = ?";
    private static final String GET_SENDER_CLIENT_TYPE_QUERY = "SELECT client_types.type FROM clients JOIN client_types ON clients.client_type_id = "
            + "client_types.id WHERE clients.id = ?";
    private static final double USD_TO_EUR_RATE = 0.92;
    private static final double EUR_TO_USD_RATE = 1.08;
    private static final TransferDaoImpl INSTANCE = new TransferDaoImpl();
    private final ConnectionPool pool = ConnectionPool.getInstance();
    public static TransferDaoImpl getInstance() {
        return INSTANCE;
    }
    private TransferDaoImpl(){}
    @Override
    public Transfer create(final Transfer transfer) {
        Connection connection = null;
        try {
            connection = pool.getConnection();
            final PreparedStatement getSenderAccountRest = connection.prepareStatement(GET_ACCOUNT_INFO_QUERY);
            getSenderAccountRest.setInt(1, transfer.getSenderAccountId());
            ResultSet resultSet = getSenderAccountRest.executeQuery();
            resultSet.next();
            final Account senderAccountInfo = createAccountInfoFromResultSet(resultSet);

            final PreparedStatement getReceiverAccountRest = connection.prepareStatement(GET_ACCOUNT_INFO_QUERY);
            getReceiverAccountRest.setInt(1, transfer.getReceiverAccountId());
            resultSet = getReceiverAccountRest.executeQuery();
            resultSet.next();
            final Account receiverAccountInfo = createAccountInfoFromResultSet(resultSet);

            int receivedMoney;
            if (isSenderBankEqualsReceiverBank(senderAccountInfo.getBankId(), receiverAccountInfo.getBankId())) {
                receivedMoney = transfer.getAmount();
            } else {
                final PreparedStatement getSenderClientTypeStatement = connection.prepareStatement(GET_SENDER_CLIENT_TYPE_QUERY);
                getSenderClientTypeStatement.setInt(1, senderAccountInfo.getClientId());
                resultSet = getSenderClientTypeStatement.executeQuery();
                final ClientType clientType = resultSet.next() ? ClientType.valueOf(resultSet.getString(1)) : ClientType.INDIVIDUAL;
                PreparedStatement getSenderBankCommission;
                if (clientType == ClientType.INDIVIDUAL) {
                    getSenderBankCommission = connection.prepareStatement(GET_BANK_COMMISSION_FOR_INDIVIDUAL_QUERY);
                } else {
                    getSenderBankCommission = connection.prepareStatement(GET_BANK_COMMISSION_FOR_LEGAL_QUERY);
                }
                getSenderBankCommission.setInt(1, senderAccountInfo.getBankId());
                resultSet = getSenderBankCommission.executeQuery();
                final int commission = resultSet.next() ? resultSet.getInt(1) : 0;

                receivedMoney = calculateAmountWithCommission(transfer.getAmount(), commission);
            }

            if (isDifferentAccountCurrencies(senderAccountInfo.getCurrency(), receiverAccountInfo.getCurrency())) {
                if ("USD".equals(senderAccountInfo.getCurrency())) {
                    receivedMoney *= USD_TO_EUR_RATE;
                } else {
                    receivedMoney *= EUR_TO_USD_RATE;
                }
            }


            final PreparedStatement sendMoneyStatement = connection.prepareStatement(SEND_MONEY_QUERY);
            sendMoneyStatement.setInt(1, senderAccountInfo.getRest() - transfer.getAmount());
            sendMoneyStatement.setInt(2, transfer.getSenderAccountId());
            int affectedRows = sendMoneyStatement.executeUpdate();
            validateAffectedRows(affectedRows);

            final PreparedStatement receiveMoneyStatement = connection.prepareStatement(RECEIVE_MONEY_QUERY);
            receiveMoneyStatement.setInt(1, receiverAccountInfo.getRest() + receivedMoney);
            receiveMoneyStatement.setInt(2, transfer.getReceiverAccountId());
            affectedRows = receiveMoneyStatement.executeUpdate();
            validateAffectedRows(affectedRows);

            final PreparedStatement statement = connection.prepareStatement(CREATE_TRANSFER_QUERY, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, transfer.getSenderAccountId());
            statement.setInt(2, transfer.getReceiverAccountId());
            statement.setInt(3, transfer.getAmount());
            statement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            affectedRows = statement.executeUpdate();
            validateAffectedRows(affectedRows);

            final ResultSet generatedKeys = statement.getGeneratedKeys();
            validateGeneratedKeys(generatedKeys);

            transfer.setId(generatedKeys.getInt(1));
            return transfer;
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
    public Optional<Transfer> getById(final int id) {
        try (Connection connection = pool.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_TRANSFER_QUERY)) {
            statement.setInt(1, id);
            final ResultSet resultSet = statement.executeQuery();
            return Optional.ofNullable(resultSet.next() ? createTransferFromResultSet(resultSet) : null);
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    @Override
    public List<Transfer> getClientTransfersForPeriod(final int clientId, final Date startDate, final Date endDate) {
        try(Connection connection = pool.getConnection();
            PreparedStatement statement = connection.prepareStatement(GET_CLIENT_TRANSFERS_FOR_PERIOD)) {
            statement.setInt(1, clientId);
            statement.setDate(2, new java.sql.Date(startDate.getTime()));
            statement.setDate(3, new java.sql.Date(endDate.getTime()));
            final ResultSet resultSet = statement.executeQuery();
            final List<Transfer> transfers = new ArrayList<>();
            while (resultSet.next()) {
                transfers.add(createTransferFromResultSet(resultSet));
            }
            return transfers;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong...", e);
        }
    }

    private int calculateAmountWithCommission(final int amount, final int commission) {
        return  (amount * (100 - commission)) / 100;
    }
    private boolean isSenderBankEqualsReceiverBank(final int senderBankId, final int receiverBankId) {
        return senderBankId == receiverBankId;
    }
    private boolean isDifferentAccountCurrencies(final String senderCurrency, final String receiverCurrency) {
        return !senderCurrency.equals(receiverCurrency);
    }

    private Transfer createTransferFromResultSet(final ResultSet resultSet) throws SQLException {
        final Transfer transfer = new Transfer();
        transfer.setId(resultSet.getInt(ID));
        transfer.setSenderAccountId(resultSet.getInt(SENDER_ACCOUNT_ID));
        transfer.setReceiverAccountId(resultSet.getInt(RECEIVER_ACCOUNT_ID));
        transfer.setAmount(resultSet.getInt(AMOUNT));
        transfer.setDate(resultSet.getDate(DATE));
        return transfer;
    }
    private Account createAccountInfoFromResultSet(final ResultSet resultSet) throws SQLException {
        final Account account = new Account();
        account.setClientId(resultSet.getInt(CLIENT_ID));
        account.setBankId(resultSet.getInt(BANK_ID));
        account.setCurrency(resultSet.getString(CURRENCY));
        account.setRest(resultSet.getInt(REST));
        return account;
    }
}
