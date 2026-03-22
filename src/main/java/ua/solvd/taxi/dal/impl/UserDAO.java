package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO implements DAO<Long, User> {

    @Override
    public User save(User user) throws SQLException {
        String findRoleIdSql = "SELECT id FROM role WHERE name = ?";
        String insertUserSql = "INSERT INTO user (first_name, last_name, phone, role_id) VALUES (?, ?, ?, ?)";
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
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        String sql = """
                 SELECT\s
                     users.first_name, users.last_name, users.phone,\s
                     roles.name AS role_name
                 FROM user AS users
                 JOIN role AS roles ON users.role_id = roles.id
                 WHERE users.id = ?
                \s""";
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
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = """
                 SELECT\s
                     users.first_name, users.last_name, users.phone,\s
                     roles.name AS role_name
                 FROM user AS users
                 JOIN role AS roles ON users.role_id = roles.id
                \s""";
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
    }

    public Optional<User> findUserByPhone(String phone) throws SQLException {
        String sql = """
                 SELECT\s
                     users.first_name, users.last_name, users.phone,\s
                     roles.name AS role_name
                 FROM user AS users
                 JOIN role AS roles ON users.role_id = roles.id
                 WHERE users.phone = ?
                \s""";
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
    }

    @Override
    public boolean update(Long id, User user) throws SQLException {
        String findRoleIdSql = "SELECT id FROM role WHERE name = ?";
        String updateUserSql = """
                 UPDATE user\s
                 SET first_name = ?, last_name = ?, phone = ?, role_id = ?\s
                 WHERE id = ?
                \s""";
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
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private User mapRowToUser(ResultSet resultSet) throws SQLException {
        String roleName = resultSet.getString("role_name");
        Role role = new Role(roleName);
        return new User(
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                role
        );
    }
}