package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.domain.model.Entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Dao<T extends Entity> {
    T save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll();

    boolean update(T entity);

    boolean delete(UUID id);
}