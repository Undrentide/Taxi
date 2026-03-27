package ua.solvd.taxi.domain.dal.jdbcimpl;

import ua.solvd.taxi.domain.dal.JDBCDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CarJDBCDAO extends JDBCDAO<Car> {

    @Override
    public Car save(Car car) {
        String findClassIdSql = "SELECT id FROM car_class WHERE name = ?";
        String insertCarSql = "INSERT INTO car (id, brand, model, license_plate, color, class_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            return execute(connection -> {
                String classId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findClassIdSql)) {
                    preparedStatement.setString(1, car.getCarClass().getName());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()) {
                        throw new SQLException("CarClass not found: " + car.getCarClass().getName());
                    }
                    classId = resultSet.getString("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertCarSql)) {
                    preparedStatement.setString(1, car.getId().toString());
                    preparedStatement.setString(2, car.getBrand());
                    preparedStatement.setString(3, car.getModel());
                    preparedStatement.setString(4, car.getLicensePlate());
                    preparedStatement.setString(5, car.getColor());
                    preparedStatement.setString(6, classId);
                    preparedStatement.executeUpdate();
                    return car;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving car.", e);
        }
    }

    @Override
    public Optional<Car> findById(UUID id) {
        String sql = getBaseSelectQuery() + " WHERE cars.id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToCar(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding car by id.", e);
        }
    }

    @Override
    public List<Car> findAll() {
        String sql = getBaseSelectQuery();
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<Car> carList = new ArrayList<>();
                    while (resultSet.next()) {
                        carList.add(mapRowToCar(resultSet));
                    }
                    return carList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all cars.", e);
        }
    }

    @Override
    public boolean update(Car car) {
        String findClassIdSql = "SELECT id FROM car_class WHERE name = ?";
        String updateSql = """
                 UPDATE car
                 SET brand = ?, model = ?, license_plate = ?, color = ?, class_id = ?
                 WHERE id = ?
                """;
        try {
            return execute(connection -> {
                String classId;
                try (PreparedStatement classStmt = connection.prepareStatement(findClassIdSql)) {
                    classStmt.setString(1, car.getCarClass().getName());
                    ResultSet resultSet = classStmt.executeQuery();
                    if (!resultSet.next()) throw new SQLException("CarClass not found");
                    classId = resultSet.getString("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
                    preparedStatement.setString(1, car.getBrand());
                    preparedStatement.setString(2, car.getModel());
                    preparedStatement.setString(3, car.getLicensePlate());
                    preparedStatement.setString(4, car.getColor());
                    preparedStatement.setString(5, classId);
                    preparedStatement.setString(6, car.getId().toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating car.", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM car WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting car.", e);
        }
    }

    private String getBaseSelectQuery() {
        return """
                 SELECT
                     cars.id AS car_id,
                     cars.brand,
                     cars.model,
                     cars.license_plate,
                     cars.color,
                     classes.id AS class_id,
                     classes.name AS class_name,
                     classes.base_price AS class_price
                 FROM car AS cars
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                """;
    }

    private Car mapRowToCar(ResultSet resultSet) throws SQLException {
        String carIdStr = resultSet.getString("car_id");
        String classIdStr = resultSet.getString("class_id");
        CarClass carClass = new CarClass(
                UUID.fromString(classIdStr),
                resultSet.getString("class_name"),
                resultSet.getBigDecimal("class_price")
        );
        return new Car(
                UUID.fromString(carIdStr),
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                carClass
        );
    }
}