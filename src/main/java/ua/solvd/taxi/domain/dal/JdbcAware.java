package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.configuration.JdbcConnectionPool;
import ua.solvd.taxi.domain.exception.PersistenceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public abstract class JdbcAware {
    private static final JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.getInstance();

    public static <R> R execute(Function<Connection, R> action) {
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            return action.apply(connection);
        } catch (SQLException e) {
            throw new PersistenceException("Database error occurred", e);
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
    }
}