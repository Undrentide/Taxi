package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.CarClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarClassDAO extends AbstractDAO implements DAO<Long, CarClass> {

    @Override
    public CarClass save(CarClass carClass) throws SQLException {
        String sql = "INSERT INTO car_class (name, base_price) VALUES (?, ?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, carClass.getName());
                preparedStatement.setBigDecimal(2, carClass.getBasePrice());
                preparedStatement.executeUpdate();
                return carClass;
            }
        });
    }

    @Override
    public Optional<CarClass> findById(Long id) throws SQLException {
        String sql = "SELECT classes.name, classes.base_price FROM car_class AS classes WHERE classes.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(mapRowToCarClass(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<CarClass> findAll() throws SQLException {
        String sql = "SELECT classes.name, classes.base_price FROM car_class AS classes";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                List<CarClass> carClassList = new ArrayList<>();
                while (resultSet.next()) {
                    carClassList.add(mapRowToCarClass(resultSet));
                }
                return carClassList;
            }
        });
    }

    @Override
    public boolean update(Long id, CarClass carClass) throws SQLException {
        String sql = "UPDATE car_class SET name = ?, base_price = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, carClass.getName());
                preparedStatement.setBigDecimal(2, carClass.getBasePrice());
                preparedStatement.setLong(3, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM car_class WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private CarClass mapRowToCarClass(ResultSet resultSet) throws SQLException {
        return new CarClass(
                resultSet.getString("name"),
                resultSet.getBigDecimal("base_price")
        );
    }
}