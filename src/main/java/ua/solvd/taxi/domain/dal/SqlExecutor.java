package ua.solvd.taxi.domain.dal;

import java.sql.SQLException;

public interface SqlExecutor<T, R> {
    R apply(T t) throws SQLException;
}