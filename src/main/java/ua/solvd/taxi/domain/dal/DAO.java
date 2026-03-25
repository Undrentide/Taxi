package ua.solvd.taxi.domain.dal;

import java.util.List;
import java.util.Optional;

public interface DAO<K, T> {
    T save(T entity);

    Optional<T> findById(K id);

    List<T> findAll();

    boolean update(K id, T entity);

    boolean delete(K id);
}