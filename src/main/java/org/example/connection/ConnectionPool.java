package org.example.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

    /**
     * Size of connection pool
     */
    private static final int DEFAULT_POOL_SIZE = 4;

    /**
     * Instance of thread safe singleton
     */
    private static ConnectionPool instance = new ConnectionPool();

    /**
     * Auxiliary object to make singleton thread safe
     */
    private static final AtomicBoolean isCreated = new AtomicBoolean(false);

    /**
     * Queue that contains free connections
     */
    private final BlockingQueue<ProxyConnection> freeConnections;

    /**
     * Queue that contains connections used by users
     */
    private final BlockingQueue<ProxyConnection> usedConnections;

    /**
     * Private constructor that initialize connection pool
     */
    private ConnectionPool(){
        freeConnections = new LinkedBlockingQueue<>(DEFAULT_POOL_SIZE);
        usedConnections = new LinkedBlockingQueue<>(DEFAULT_POOL_SIZE);
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++){
            try{
                Connection connection = ConnectionFactory.createConnection();
                ProxyConnection proxyConnection = new ProxyConnection(connection);
                freeConnections.put(proxyConnection);
            } catch (SQLException e) {
                LOGGER.error("Unable to create connection: " + e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (freeConnections.isEmpty()){
            LOGGER.error("Unable to create all connections");
            throw new RuntimeException("Unable to create all connections");
        }
    }

    /**
     * Returns the instance of {@link ConnectionPool}
     * @return {@link ConnectionPool} object
     */
    public static ConnectionPool getInstance(){
        while (instance == null){
            if (isCreated.compareAndSet(false, true)){
                instance = new ConnectionPool();
            }
        }
        return instance;
    }

    /**
     * Returns free {@link Connection} to user
     * @return free {@link ProxyConnection} from freeConnections queue
     */
    public Connection getConnection(){
        ProxyConnection connection = null;
        try{
            connection = freeConnections.take();
            usedConnections.put(connection);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    /**
     * Releases used {@link Connection} to {@link ConnectionPool}
     * @param connection - released connection
     * @return true if it's possible to release connection, false otherwise
     */
    public boolean releaseConnection(Connection connection){
        if (!(connection instanceof ProxyConnection)){
            LOGGER.error("Incorrect connection: " + connection);
            return false;
        }
        usedConnections.remove(connection);
        try {
            freeConnections.put((ProxyConnection) connection);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    /**
     * Destroys {@link ConnectionPool}
     */
    public void destroy(){
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++){
            try{
                freeConnections.take().reallyClose();
            } catch (InterruptedException | SQLException e) {
                LOGGER.error("Connection wasn't deleted");
            }
        }
        deregisterDrivers();
    }

    /**
     * Deregister database drivers
     */
    private void deregisterDrivers(){
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()){
            Driver driver = drivers.nextElement();
            try{
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                LOGGER.error("Unable to deregister driver: " + driver, e);
            }
        }
    }
}
