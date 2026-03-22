package ua.solvd.taxi.dal;

import java.sql.SQLException;

public interface SqlExecutor<T, R> {
    R apply(T t) throws SQLException;
}