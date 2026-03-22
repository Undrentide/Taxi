package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.DriverLocation;
import ua.solvd.taxi.domain.model.impl.DriverStatus;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverLocationDAO extends AbstractDAO implements DAO<Long, DriverLocation> {

    @Override
    public DriverLocation save(DriverLocation driverLocation) throws SQLException {
        String findDriverIdSql = """
                 SELECT drivers.id
                 FROM driver AS drivers
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 WHERE users.phone = ?
                """;
        String insertSql = """
                 INSERT INTO driver_location_log (driver_id, latitude, longitude, updated_at)
                 VALUES (?, ?, ?, ?)
                """;
        return execute(connection -> {
            long driverId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findDriverIdSql)) {
                preparedStatement.setString(1, driverLocation.getDriver().getUser().getPhone());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    driverId = resultSet.getLong("id");
                } else {
                    throw new SQLException("Driver not found for location log");
                }
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                preparedStatement.setLong(1, driverId);
                preparedStatement.setDouble(2, driverLocation.getLatitude());
                preparedStatement.setDouble(3, driverLocation.getLongitude());
                preparedStatement.setTimestamp(4, Timestamp.from(driverLocation.getUpdatedAt()));
                preparedStatement.executeUpdate();
                return driverLocation;
            }
        });
    }

    @Override
    public Optional<DriverLocation> findById(Long id) throws SQLException {
        String sql = """
                 SELECT
                     logs.latitude, logs.longitude, logs.updated_at,
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver_location_log AS logs
                 INNER JOIN driver AS drivers ON logs.driver_id = drivers.id
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 INNER JOIN car AS cars ON drivers.car_id = cars.id
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 INNER JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                 WHERE logs.id = ?
                """;
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(mapRowToLocation(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<DriverLocation> findAll() throws SQLException {
        String sql = """
                 SELECT
                     logs.latitude, logs.longitude, logs.updated_at,
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver_location_log AS logs
                 INNER JOIN driver AS drivers ON logs.driver_id = drivers.id
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 INNER JOIN car AS cars ON drivers.car_id = cars.id
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 INNER JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                """;
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                List<DriverLocation> driverLocationList = new ArrayList<>();
                while (resultSet.next()) {
                    driverLocationList.add(mapRowToLocation(resultSet));
                }
                return driverLocationList;
            }
        });
    }

    @Override
    public boolean update(Long id, DriverLocation driverLocation) throws SQLException {
        String sql = "UPDATE driver_location_log SET latitude = ?, longitude = ?, updated_at = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setDouble(1, driverLocation.getLatitude());
                preparedStatement.setDouble(2, driverLocation.getLongitude());
                preparedStatement.setTimestamp(3, Timestamp.from(driverLocation.getUpdatedAt()));
                preparedStatement.setLong(4, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM driver_location_log WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private DriverLocation mapRowToLocation(ResultSet resultSet) throws SQLException {
        Role role = new Role(resultSet.getString("role_name"));
        User user = new User(
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                role
        );
        CarClass carClass = new CarClass(
                resultSet.getString("class_name"),
                resultSet.getBigDecimal("class_price")
        );
        Car car = new Car(
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                carClass
        );
        DriverStatus status = new DriverStatus(resultSet.getString("status_name"));
        Driver driver = new Driver(user, car, status, resultSet.getBigDecimal("rating"));
        return new DriverLocation(
                driver,
                resultSet.getDouble("latitude"),
                resultSet.getDouble("longitude"),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }
}