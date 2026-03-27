package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.domain.model.impl.User;

import java.util.Optional;

public interface UserDAO<K> extends DAO<K, User> {
    Optional<User> findUserByPhone(String phone);
}