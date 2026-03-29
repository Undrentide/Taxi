package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.CarDao;
import ua.solvd.taxi.domain.dal.JdbcAware;
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

public class CarJdbcDao extends JdbcAware implements CarDao {

    private static final class SqlQuery {
        static final String FIND_CLASS_ID = "SELECT id FROM car_class WHERE name = ?";

        static final String INSERT = "INSERT INTO car (id, brand, model, license_plate, color, class_id) VALUES (?, ?, ?, ?, ?, ?)";

        static final String UPDATE = """
                 UPDATE car
                 SET brand = ?, model = ?, license_plate = ?, color = ?, class_id = ?
                 WHERE id = ?
                """;

        static final String DELETE = "DELETE FROM car WHERE id = ?";

        static final String BASE_SELECT = """
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

        static final String FIND_BY_ID = BASE_SELECT + " WHERE cars.id = ?";
    }

    @Override
    public Car save(Car car) {
        return execute(connection -> {
            String classId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_CLASS_ID)) {
                preparedStatement.setString(1, car.getCarClass().getName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("CarClass not found: " + car.getCarClass().getName());
                    }
                    classId = resultSet.getString("id");
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding car class.", e);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.INSERT)) {
                preparedStatement.setString(1, car.getId().toString());
                preparedStatement.setString(2, car.getBrand());
                preparedStatement.setString(3, car.getModel());
                preparedStatement.setString(4, car.getLicensePlate());
                preparedStatement.setString(5, car.getColor());
                preparedStatement.setString(6, classId);
                preparedStatement.executeUpdate();
                return car;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while saving car.", e);
            }
        });
    }

    @Override
    public Optional<Car> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_ID)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next() ? Optional.of(mapRowToCar(resultSet)) : Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding car by id.", e);
            }
        });
    }

    @Override
    public List<Car> findAll() {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.BASE_SELECT);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Car> carList = new ArrayList<>();
                while (resultSet.next()) {
                    carList.add(mapRowToCar(resultSet));
                }
                return carList;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding all cars.", e);
            }
        });

    }

    @Override
    public boolean update(Car car) {
        return execute(connection -> {
            String classId;
            try (PreparedStatement prepareStatement = connection.prepareStatement(SqlQuery.FIND_CLASS_ID)) {
                prepareStatement.setString(1, car.getCarClass().getName());
                try (ResultSet resultSet = prepareStatement.executeQuery()) {
                    if (!resultSet.next()) throw new SQLException("CarClass not found");
                    classId = resultSet.getString("id");
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding car class.", e);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.UPDATE)) {
                preparedStatement.setString(1, car.getBrand());
                preparedStatement.setString(2, car.getModel());
                preparedStatement.setString(3, car.getLicensePlate());
                preparedStatement.setString(4, car.getColor());
                preparedStatement.setString(5, classId);
                preparedStatement.setString(6, car.getId().toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while updating car.", e);
            }
        });
    }

    @Override
    public boolean delete(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.DELETE)) {
                preparedStatement.setString(1, id.toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while deleting car.", e);
            }
        });
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