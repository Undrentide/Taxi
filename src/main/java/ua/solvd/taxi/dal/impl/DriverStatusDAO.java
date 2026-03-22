package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.DriverStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverStatusDAO extends AbstractDAO implements DAO<Long, DriverStatus> {

    @Override
    public DriverStatus save(DriverStatus status) throws SQLException {
        String sql = "INSERT INTO driver_status (name) VALUES (?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, status.getName());
                preparedStatement.executeUpdate();
                return status;
            }
        });
    }

    @Override
    public Optional<DriverStatus> findById(Long id) throws SQLException {
        String sql = "SELECT status.name FROM driver_status AS status WHERE status.id = ?";
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
    }

    @Override
    public List<DriverStatus> findAll() throws SQLException {
        String sql = "SELECT status.name FROM driver_status AS status";
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
    }

    @Override
    public boolean update(Long id, DriverStatus status) throws SQLException {
        String sql = "UPDATE driver_status SET name = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, status.getName());
                preparedStatement.setLong(2, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM driver_status WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private DriverStatus mapRowToStatus(ResultSet resultSet) throws SQLException {
        return new DriverStatus(resultSet.getString("name"));
    }
}