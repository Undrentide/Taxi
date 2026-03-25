package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDAOUtil implements DAO<Long, Role> {

    @Override
    public Role save(Role role) {
        String sql = "INSERT INTO role (name) VALUES (?)";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, role.getName());
                    preparedStatement.executeUpdate();
                    return role;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving role.", e);
        }
    }

    @Override
    public Optional<Role> findById(Long id) {
        String sql = "SELECT r.name FROM role AS r WHERE r.id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToRole(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding role by id.", e);
        }
    }

    @Override
    public List<Role> findAll() {
        String sql = "SELECT r.name FROM role AS r";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<Role> roleList = new ArrayList<>();
                    while (resultSet.next()) {
                        roleList.add(mapRowToRole(resultSet));
                    }
                    return roleList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all roles.", e);
        }
    }

    @Override
    public boolean update(Long id, Role role) {
        String sql = "UPDATE role SET name = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, role.getName());
                    preparedStatement.setLong(2, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating role.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM role WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting role", e);
        }
    }

    private Role mapRowToRole(ResultSet resultSet) {
        try {
            return new Role(resultSet.getString("name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping role.", e);
        }
    }
}