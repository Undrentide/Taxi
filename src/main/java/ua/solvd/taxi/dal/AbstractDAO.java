package ua.solvd.taxi.dal;

import ua.solvd.taxi.configuration.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDAO {
    protected final JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.getInstance();

    protected <R> R execute(SqlExecutor<Connection, R> action) throws SQLException {
        Connection connection = jdbcConnectionPool.getConnection();
        try {
            return action.apply(connection);
        } finally {
            jdbcConnectionPool.releaseConnection(connection);
        }
    }
}