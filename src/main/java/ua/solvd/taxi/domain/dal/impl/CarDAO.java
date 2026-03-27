package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDAO implements DAO<Long, Car> {

    @Override
    public Car save(Car car) {
        String findClassIdSql = "SELECT id FROM car_class WHERE name = ?";
        String insertCarSql = "INSERT INTO car (brand, model, license_plate, color, class_id) VALUES (?, ?, ?, ?, ?)";
        try {
            return DAOUtil.execute(connection -> {
                long classId;
                try (PreparedStatement preparedStatement = connection.prepareStatement(findClassIdSql)) {
                    preparedStatement.setString(1, car.getCarClass().getName());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (!resultSet.next()) {
                        throw new SQLException("CarClass not found: " + car.getCarClass().getName());
                    }
                    classId = resultSet.getLong("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertCarSql)) {
                    preparedStatement.setString(1, car.getBrand());
                    preparedStatement.setString(2, car.getModel());
                    preparedStatement.setString(3, car.getLicensePlate());
                    preparedStatement.setString(4, car.getColor());
                    preparedStatement.setLong(5, classId);
                    preparedStatement.executeUpdate();
                    return car;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving car.", e);
        }
    }

    @Override
    public Optional<Car> findById(Long id) {
        String sql = """
                 SELECT
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name,
                     classes.base_price AS class_price
                 FROM car AS cars
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                 WHERE cars.id = ?
                """;
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
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
        String sql = """
                 SELECT
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name,
                     classes.base_price AS class_price
                 FROM car AS cars
                 INNER JOIN car_class AS classes ON cars.class_id = classes.id
                """;
        try {
            return DAOUtil.execute(connection -> {
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
    public boolean update(Long id, Car car) {
        String findClassIdSql = "SELECT id FROM car_class WHERE name = ?";
        String updateSql = """
                 UPDATE car
                 SET brand = ?, model = ?, license_plate = ?, color = ?, class_id = ?
                 WHERE id = ?
                """;
        try {
            return DAOUtil.execute(connection -> {
                long classId;
                try (PreparedStatement classStmt = connection.prepareStatement(findClassIdSql)) {
                    classStmt.setString(1, car.getCarClass().getName());
                    ResultSet resultSet = classStmt.executeQuery();
                    if (!resultSet.next()) throw new SQLException("CarClass not found");
                    classId = resultSet.getLong("id");
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
                    preparedStatement.setString(1, car.getBrand());
                    preparedStatement.setString(2, car.getModel());
                    preparedStatement.setString(3, car.getLicensePlate());
                    preparedStatement.setString(4, car.getColor());
                    preparedStatement.setLong(5, classId);
                    preparedStatement.setLong(6, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating car.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM car WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting car.", e);
        }
    }

    private Car mapRowToCar(ResultSet resultSet) {
        CarClass carClass;
        try {
            carClass = new CarClass(
                    resultSet.getString("class_name"),
                    resultSet.getBigDecimal("class_price")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping car.", e);
        }
        try {
            return new Car(
                    resultSet.getString("brand"),
                    resultSet.getString("model"),
                    resultSet.getString("license_plate"),
                    resultSet.getString("color"),
                    carClass
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping car.", e);
        }
    }
}