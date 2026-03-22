package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Car;
import ua.solvd.taxi.model.impl.CarClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDAO extends AbstractDAO implements DAO<Long, Car> {

    @Override
    public Car save(Car car) throws SQLException {
        String findClassIdSql = "SELECT id FROM car_class WHERE name = ?";
        String insertCarSql = "INSERT INTO car (brand, model, license_plate, color, class_id) VALUES (?, ?, ?, ?, ?)";
        return execute(connection -> {
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
    }

    @Override
    public Optional<Car> findById(Long id) throws SQLException {
        String sql = """
                 SELECT\s
                     cars.brand, cars.model, cars.license_plate, cars.color,\s
                     classes.name AS class_name,\s
                     classes.base_price AS class_price
                 FROM car AS cars
                 JOIN car_class AS classes ON cars.class_id = classes.id
                 WHERE cars.id = ?
                \s""";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(mapRowToCar(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Car> findAll() throws SQLException {
        String sql = """
                 SELECT\s
                     cars.brand, cars.model, cars.license_plate, cars.color,\s
                     classes.name AS class_name,\s
                     classes.base_price AS class_price
                 FROM car AS cars
                 JOIN car_class AS classes ON cars.class_id = classes.id
                \s""";
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
    }

    @Override
    public boolean update(Long id, Car car) throws SQLException {
        String findClassIdSql = "SELECT id FROM car_class WHERE name = ?";
        String updateSql = """
                 UPDATE car\s
                 SET brand = ?, model = ?, license_plate = ?, color = ?, class_id = ?\s
                 WHERE id = ?
                \s""";
        return execute(connection -> {
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
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM car WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private Car mapRowToCar(ResultSet resultSet) throws SQLException {
        CarClass carClass = new CarClass(
                resultSet.getString("class_name"),
                resultSet.getBigDecimal("class_price")
        );
        return new Car(
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                carClass
        );
    }
}