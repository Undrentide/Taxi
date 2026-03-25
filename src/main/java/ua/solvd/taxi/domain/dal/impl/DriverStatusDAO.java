package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.DriverStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverStatusDAO extends AbstractDAO implements DAO<Long, DriverStatus> {

    @Override
    public DriverStatus save(DriverStatus status) {
        String sql = "INSERT INTO driver_status (name) VALUES (?)";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, status.getName());
                    preparedStatement.executeUpdate();
                    return status;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving driver status.", e);
        }
    }

    @Override
    public Optional<DriverStatus> findById(Long id) {
        String sql = "SELECT status.name FROM driver_status AS status WHERE status.id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToStatus(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding driver status by id.", e);
        }
    }

    @Override
    public List<DriverStatus> findAll() {
        String sql = "SELECT status.name FROM driver_status AS status";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<DriverStatus> statusList = new ArrayList<>();
                    while (resultSet.next()) {
                        statusList.add(mapRowToStatus(resultSet));
                    }
                    return statusList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all driver statuses.", e);
        }
    }

    @Override
    public boolean update(Long id, DriverStatus status) {
        String sql = "UPDATE driver_status SET name = ? WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, status.getName());
                    preparedStatement.setLong(2, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating driver status.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM driver_status WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting driver status.", e);
        }
    }

    private DriverStatus mapRowToStatus(ResultSet resultSet) {
        try {
            return new DriverStatus(resultSet.getString("name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver status.", e);
        }
    }
}