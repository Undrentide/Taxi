package ua.solvd.taxi.domain.dal.jdbcimpl;

import ua.solvd.taxi.domain.dal.JdbcDao;
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
import java.util.UUID;

public class UserJdbcDao extends JdbcDao<User> {

    private static final class SqlQuery {
        static final String FIND_ROLE_ID = "SELECT id FROM role WHERE name = ?";

        static final String INSERT = "INSERT INTO user (id, first_name, last_name, phone, role_id) VALUES (?, ?, ?, ?, ?)";

        static final String UPDATE = """
                 UPDATE user
                 SET first_name = ?, last_name = ?, phone = ?, role_id = ?
                 WHERE id = ?
                """;

        static final String DELETE = "DELETE FROM user WHERE id = ?";

        static final String BASE_SELECT = """
                 SELECT
                     users.id, users.first_name, users.last_name, users.phone,
                     roles.id AS role_id, roles.name AS role_name
                 FROM user AS users
                 INNER JOIN role AS roles ON users.role_id = roles.id
                """;

        static final String FIND_BY_ID = BASE_SELECT + " WHERE users.id = ?";

        static final String FIND_BY_PHONE = BASE_SELECT + " WHERE users.phone = ?";
    }

    @Override
    public User save(User user) {
        return execute(connection -> {
            String roleId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_ROLE_ID)) {
                preparedStatement.setString(1, user.getRole().getName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Role '" + user.getRole().getName() + "' not found in DB.");
                    }
                    roleId = resultSet.getString("id");
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding role.", e);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.INSERT, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getId().toString());
                preparedStatement.setString(2, user.getFirstName());
                preparedStatement.setString(3, user.getLastName());
                preparedStatement.setString(4, user.getPhone());
                preparedStatement.setString(5, roleId);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }
                return user;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while saving user.", e);
            }
        });
    }

    @Override
    public Optional<User> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_ID)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToUser(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding user by id.", e);
            }
        });
    }

    @Override
    public List<User> findAll() {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.BASE_SELECT);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                List<User> userList = new ArrayList<>();
                while (resultSet.next()) {
                    userList.add(mapRowToUser(resultSet));
                }
                return userList;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding all users.", e);
            }
        });
    }

    public Optional<User> findUserByPhone(String phone) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_PHONE)) {
                preparedStatement.setString(1, phone);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToUser(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding user by phone.", e);
            }
        });
    }

    @Override
    public boolean update(User user) {
        return execute(connection -> {
            String roleId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_ROLE_ID)) {
                preparedStatement.setString(1, user.getRole().getName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Role '" + user.getRole().getName() + "' not found");
                    }
                    roleId = resultSet.getString("id");
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding role.", e);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.UPDATE)) {
                preparedStatement.setString(1, user.getFirstName());
                preparedStatement.setString(2, user.getLastName());
                preparedStatement.setString(3, user.getPhone());
                preparedStatement.setString(4, roleId);
                preparedStatement.setString(5, user.getId().toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while updating user.", e);
            }
        });
    }

    @Override
    public boolean delete(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.DELETE)) {
                preparedStatement.setString(1, id.toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while deleting user.", e);
            }
        });
    }

    private User mapRowToUser(ResultSet resultSet) throws SQLException {
        UUID userId = UUID.fromString(resultSet.getString("id"));
        UUID roleId = UUID.fromString(resultSet.getString("role_id"));
        String roleName = resultSet.getString("role_name");
        Role role = new Role(roleId, roleName);
        return new User(
                userId,
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                role
        );
    }
}