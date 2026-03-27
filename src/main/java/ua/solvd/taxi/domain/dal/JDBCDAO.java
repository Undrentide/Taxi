package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.configuration.JdbcConnectionPool;
import ua.solvd.taxi.domain.model.Entity;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class JDBCDAO<T extends Entity> implements DAO<T> {
    private static final JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.getInstance();

    public static <R> R execute(SqlExecutor<Connection, R> action) throws SQLException {
        Connection connection = jdbcConnectionPool.getConnection();
        try {
            return action.apply(connection);
        } finally {
            jdbcConnectionPool.releaseConnection(connection);
        }
    }
}