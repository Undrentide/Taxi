package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
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

public class DriverDAO extends AbstractDAO implements DAO<Long, Driver> {

    @Override
    public Driver save(Driver driver) {
        String findIdsSql = """    
                SELECT
                          u.id AS user_id,
                          c.id AS car_id,
                          ds.id AS status_id
                      FROM user AS u
                      INNER JOIN car AS c ON c.license_plate = ?
                      INNER JOIN driver_status AS ds ON ds.name = ?
                      WHERE u.phone = ?""";
        String insertDriverSql = """
                 INSERT INTO driver (user_id, car_id, status_id, rating)
                 VALUES (?, ?, ?, ?)
                """;
        try {
            return execute(connection -> {
                long userId, carId, statusId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findIdsSql)) {
                    preparedStatement.setString(1, driver.getUser().getPhone());
                    preparedStatement.setString(2, driver.getCar().getLicensePlate());
                    preparedStatement.setString(3, driver.getDriverStatus().getName());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            userId = resultSet.getLong("user_id");
                            carId = resultSet.getLong("car_id");
                            statusId = resultSet.getLong("status_id");
                        } else {
                            throw new SQLException("Could not find related entities (User, Car or Status) to create Driver.");
                        }
                    }
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertDriverSql)) {
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setLong(2, carId);
                    preparedStatement.setLong(3, statusId);
                    preparedStatement.setBigDecimal(4, driver.getRating());
                    preparedStatement.executeUpdate();
                    return driver;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving driver.", e);
        }
    }

    @Override
    public Optional<Driver> findById(Long id) {
        String sql = """
                 SELECT
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver AS drivers
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 INNER JOIN car AS cars ON drivers.car_id = cars.id
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 INNER JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                 WHERE drivers.id = ?
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
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
        String sql = """
                 SELECT
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver AS drivers
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 INNER JOIN car AS cars ON drivers.car_id = cars.id
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 INNER JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                """;
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
        String sql = """
                 SELECT
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver AS drivers
                 INNER JOIN user AS users ON drivers.user_id = users.id
                 INNER JOIN role AS roles ON users.role_id = roles.id
                 INNER JOIN car AS cars ON drivers.car_id = cars.id
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 INNER JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                 WHERE statuses.name = 'Available'
                 LIMIT 1
                """;
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToDriver(resultSet));
                        }
                        return Optional.empty();
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding available driver.", e);
        }
    }

    @Override
    public boolean update(Long id, Driver driver) {
        String findStatusIdSql = "SELECT id FROM driver_status WHERE name = ?";
        String updateSql = "UPDATE driver SET status_id = ?, rating = ? WHERE id = ?";
        try {
            return execute(connection -> {
                long statusId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findStatusIdSql)) {
                    preparedStatement.setString(1, driver.getDriverStatus().getName());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()) throw new SQLException("Status not found");
                    statusId = resultSet.getLong("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
                    preparedStatement.setLong(1, statusId);
                    preparedStatement.setBigDecimal(2, driver.getRating());
                    preparedStatement.setLong(3, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating driver.", e);
        }
    }

    public boolean updateStatusByPhone(String phone, String statusName) {
        String sql = """
                     UPDATE driver
                     SET status_id = (SELECT id FROM driver_status WHERE name = ?)\s
                     WHERE user_id = (SELECT id FROM user WHERE phone = ?)
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
            throw new PersistenceException("Error occurred while updating driver status by phone", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM driver WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting driver.", e);
        }
    }

    private Driver mapRowToDriver(ResultSet resultSet) {
        Role role;
        try {
            role = new Role(resultSet.getString("role_name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver.", e);
        }
        User user;
        try {
            user = new User(
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("phone"),
                    role
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver.", e);
        }
        CarClass carClass;
        try {
            carClass = new CarClass(
                    resultSet.getString("class_name"),
                    resultSet.getBigDecimal("class_price")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver.", e);
        }
        Car car;
        try {
            car = new Car(
                    resultSet.getString("brand"),
                    resultSet.getString("model"),
                    resultSet.getString("license_plate"),
                    resultSet.getString("color"),
                    carClass
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver.", e);
        }
        DriverStatus status;
        try {
            status = new DriverStatus(resultSet.getString("status_name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver.", e);
        }
        try {
            return new Driver(user, car, status, resultSet.getBigDecimal("rating"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping driver.", e);
        }
    }
}