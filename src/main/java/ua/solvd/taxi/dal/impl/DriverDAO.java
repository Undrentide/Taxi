package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Car;
import ua.solvd.taxi.model.impl.CarClass;
import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.DriverStatus;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DriverDAO extends AbstractDAO implements DAO<Long, Driver> {

    @Override
    public Driver save(Driver driver) throws SQLException {
        String findIdsSql = """
                 SELECT\s
                     (SELECT id FROM user WHERE phone = ?) AS user_id,
                     (SELECT id FROM car WHERE license_plate = ?) AS car_id,
                     (SELECT id FROM driver_status WHERE name = ?) AS status_id
                \s""";
        String insertDriverSql = """
                 INSERT INTO driver (user_id, car_id, status_id, rating)\s
                 VALUES (?, ?, ?, ?)
                \s""";
        return execute(connection -> {
            long userId, carId, statusId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findIdsSql)) {
                preparedStatement.setString(1, driver.getUser().getPhone());
                preparedStatement.setString(2, driver.getCar().getLicensePlate());
                preparedStatement.setString(3, driver.getDriverStatus().getName());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getLong("user_id");
                    carId = resultSet.getLong("car_id");
                    statusId = resultSet.getLong("status_id");
                } else {
                    throw new SQLException("Related entities not found for Driver");
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
    }

    @Override
    public Optional<Driver> findById(Long id) throws SQLException {
        String sql = """
                 SELECT\s
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver AS drivers
                 JOIN user AS users ON drivers.user_id = users.id
                 JOIN role AS roles ON users.role_id = roles.id
                 JOIN car AS cars ON drivers.car_id = cars.id
                 JOIN car_class AS classes ON cars.class_id = classes.id
                 JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                 WHERE drivers.id = ?
                \s""";
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
    }

    @Override
    public List<Driver> findAll() throws SQLException {
        String sql = """
                 SELECT\s
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver AS drivers
                 JOIN user AS users ON drivers.user_id = users.id
                 JOIN role AS roles ON users.role_id = roles.id
                 JOIN car AS cars ON drivers.car_id = cars.id
                 JOIN car_class AS classes ON cars.class_id = classes.id
                 JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                \s""";
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
    }

    public Optional<Driver> findAvailableDriver() throws SQLException {
        String sql = """
                 SELECT\s
                     drivers.rating,
                     users.first_name, users.last_name, users.phone,
                     roles.name AS role_name,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     statuses.name AS status_name
                 FROM driver AS drivers
                 JOIN user AS users ON drivers.user_id = users.id
                 JOIN role AS roles ON users.role_id = roles.id
                 JOIN car AS cars ON drivers.car_id = cars.id
                 JOIN car_class AS classes ON cars.class_id = classes.id
                 JOIN driver_status AS statuses ON drivers.status_id = statuses.id
                 WHERE statuses.name = 'Available'
                 LIMIT 1
                \s""";
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
    }

    @Override
    public boolean update(Long id, Driver driver) throws SQLException {
        String findStatusIdSql = "SELECT id FROM driver_status WHERE name = ?";
        String updateSql = "UPDATE driver SET status_id = ?, rating = ? WHERE id = ?";
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
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM driver WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private Driver mapRowToDriver(ResultSet resultSet) throws SQLException {
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
        return new Driver(user, car, status, resultSet.getBigDecimal("rating"));
    }
}