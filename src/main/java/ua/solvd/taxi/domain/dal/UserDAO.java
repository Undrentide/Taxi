package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.domain.model.impl.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO extends DAO<Long, User> {
    Optional<User> findUserByPhone(String phone) throws SQLException;
}