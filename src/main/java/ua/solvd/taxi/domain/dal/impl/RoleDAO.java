package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.model.impl.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDAO extends AbstractDAO implements DAO<Long, Role> {

    @Override
    public Role save(Role role) throws SQLException {
        String sql = "INSERT INTO role (name) VALUES (?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, role.getName());
                preparedStatement.executeUpdate();
                return role;
            }
        });
    }

    @Override
    public Optional<Role> findById(Long id) throws SQLException {
        String sql = "SELECT r.name FROM role AS r WHERE r.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(mapRowToRole(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Role> findAll() throws SQLException {
        String sql = "SELECT r.name FROM role AS r";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                List<Role> roleList = new ArrayList<>();
                while (resultSet.next()) {
                    roleList.add(mapRowToRole(resultSet));
                }
                return roleList;
            }
        });
    }

    @Override
    public boolean update(Long id, Role role) throws SQLException {
        String sql = "UPDATE role SET name = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, role.getName());
                preparedStatement.setLong(2, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM role WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private Role mapRowToRole(ResultSet resultSet) throws SQLException {
        return new Role(resultSet.getString("name"));
    }
}