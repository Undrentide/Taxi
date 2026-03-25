package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO implements DAO<Long, User> {

    @Override
    public User save(User user) {
        String findRoleIdSql = "SELECT id FROM role WHERE name = ?";
        String insertUserSql = "INSERT INTO user (first_name, last_name, phone, role_id) VALUES (?, ?, ?, ?)";
        try {
            return execute(connection -> {
                long roleId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findRoleIdSql)) {
                    preparedStatement.setString(1, user.getRole().getName());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()) {
                        throw new SQLException("Role '" + user.getRole().getName() + "' not found in DB.");
                    }
                    roleId = resultSet.getLong("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, user.getFirstName());
                    preparedStatement.setString(2, user.getLastName());
                    preparedStatement.setString(3, user.getPhone());
                    preparedStatement.setLong(4, roleId);
                    int affectedRows = preparedStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating user failed, no rows affected.");
                    }
                    return user;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving user.", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = """
                 SELECT
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name
                 FROM user AS users
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 WHERE users.id = ?
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToUser(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding user by id.", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = """
                 SELECT
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name
                 FROM user AS users
                 INNER JOIN role AS roles ON users.role_id = roles.id
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<User> userList = new ArrayList<>();
                    while (resultSet.next()) {
                        userList.add(mapRowToUser(resultSet));
                    }
                    return userList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all users.", e);
        }
    }

    public Optional<User> findUserByPhone(String phone) {
        String sql = """
                 SELECT
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name
                 FROM user AS users
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 WHERE users.phone = ?
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, phone);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToUser(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding user by phone.", e);
        }
    }

    @Override
    public boolean update(Long id, User user) {
        String findRoleIdSql = "SELECT id FROM role WHERE name = ?";
        String updateUserSql = """
                 UPDATE user
                 SET first_name = ?, last_name = ?, phone = ?, role_id = ?
                 WHERE id = ?
                """;
        try {
            return execute(connection -> {
                long roleId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findRoleIdSql)) {
                    preparedStatement.setString(1, user.getRole().getName());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()) {
                        throw new SQLException("Role '" + user.getRole().getName() + "' not found");
                    }
                    roleId = resultSet.getLong("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateUserSql)) {
                    preparedStatement.setString(1, user.getFirstName());
                    preparedStatement.setString(2, user.getLastName());
                    preparedStatement.setString(3, user.getPhone());
                    preparedStatement.setLong(4, roleId);
                    preparedStatement.setLong(5, id);
                    int affectedRows = preparedStatement.executeUpdate();
                    return affectedRows > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating user.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting user.", e);
        }
    }

    private User mapRowToUser(ResultSet resultSet) {
        String roleName;
        try {
            roleName = resultSet.getString("role_name");
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping user.", e);
        }
        Role role = new Role(roleName);
        try {
            return new User(
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("phone"),
                    role
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping user.", e);
        }
    }
}