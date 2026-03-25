package ua.solvd.taxi.util;

import ua.solvd.taxi.configuration.JdbcConnectionPool;
import ua.solvd.taxi.domain.dal.SqlExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class DAOUtil {
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