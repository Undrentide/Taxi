package ua.solvd.taxi.domain.dal.jdbcimpl;

import ua.solvd.taxi.domain.dal.JDBCDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.DriverStatus;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DriverJDBCDAO extends JDBCDAO<Driver> {

    @Override
    public Driver save(Driver driver) {
        String insertDriverSql = """
                 INSERT INTO driver (id, user_id, car_id, status_id, rating)
                 VALUES (?, ?, ?, ?, ?)
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertDriverSql)) {
                    preparedStatement.setString(1, driver.getId().toString());
                    preparedStatement.setString(2, driver.getUser().getId().toString());
                    preparedStatement.setString(3, driver.getCar().getId().toString());
                    preparedStatement.setString(4, driver.getDriverStatus().getId().toString());
                    preparedStatement.setBigDecimal(5, driver.getRating());
                    preparedStatement.executeUpdate();
                    return driver;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving driver.", e);
        }
    }

    @Override
    public Optional<Driver> findById(UUID id) {
        String sql = getBaseSelectQuery() + " WHERE drivers.id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToDriver(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding driver by id.", e);
        }
    }

    @Override
    public List<Driver> findAll() {
        String sql = getBaseSelectQuery();
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<Driver> driverList = new ArrayList<>();
                    while (resultSet.next()) {
                        driverList.add(mapRowToDriver(resultSet));
                    }
                    return driverList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all drivers.", e);
        }
    }

    public Optional<Driver> findAvailableDriver() {
        String sql = getBaseSelectQuery() + " WHERE statuses.name = 'Available' LIMIT 1";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToDriver(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding available driver.", e);
        }
    }

    @Override
    public boolean update(Driver driver) {
        String updateSql = "UPDATE driver SET status_id = ?, rating = ? WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
                    preparedStatement.setString(1, driver.getDriverStatus().getId().toString());
                    preparedStatement.setBigDecimal(2, driver.getRating());
                    preparedStatement.setString(3, driver.getId().toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating driver.", e);
        }
    }

    public boolean updateStatusByPhone(String phone, String statusName) {
        String sql = """
                UPDATE driver AS d
                INNER JOIN user AS u ON d.user_id = u.id
                SET d.status_id = (SELECT id FROM driver_status WHERE name = ?)
                WHERE u.phone = ?
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, statusName);
                    preparedStatement.setString(2, phone);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating driver status by phone: " + phone, e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM driver WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting driver.", e);
        }
    }

    private String getBaseSelectQuery() {
        return """
                 SELECT
                     drivers.id AS driver_id, drivers.rating,
                     users.id AS user_id, users.first_name, users.last_name, users.phone,
                     roles.id AS role_id, roles.name AS role_name,
                     cars.id AS car_id, cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.id AS class_id, classes.name AS class_name, classes.base_price AS class_price,
                     statuses.id AS status_id, statuses.name AS status_name
                 FROM driver AS drivers
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 INNER JOIN car AS cars ON drivers.car_id = cars.id
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 INNER JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                """;
    }

    private Driver mapRowToDriver(ResultSet resultSet) throws SQLException {
        Role role = new Role(
                UUID.fromString(resultSet.getString("role_id")),
                resultSet.getString("role_name")
        );
        User user = new User(
                UUID.fromString(resultSet.getString("user_id")),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                role
        );
        CarClass carClass = new CarClass(
                UUID.fromString(resultSet.getString("class_id")),
                resultSet.getString("class_name"),
                resultSet.getBigDecimal("class_price")
        );
        Car car = new Car(
                UUID.fromString(resultSet.getString("car_id")),
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                carClass
        );
        DriverStatus status = new DriverStatus(
                UUID.fromString(resultSet.getString("status_id")),
                resultSet.getString("status_name")
        );
        return new Driver(
                UUID.fromString(resultSet.getString("driver_id")),
                user,
                car,
                status,
                resultSet.getBigDecimal("rating")
        );
    }
}