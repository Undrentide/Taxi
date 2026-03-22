package ua.solvd.taxi.domain.dal;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DAO<K, T> {
    T save(T entity) throws SQLException;

    Optional<T> findById(K id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean update(K id, T entity) throws SQLException;

    boolean delete(K id) throws SQLException;
}