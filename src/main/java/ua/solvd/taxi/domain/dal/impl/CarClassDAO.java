package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.CarClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarClassDAO implements DAO<Long, CarClass> {

    @Override
    public CarClass save(CarClass carClass) {
        String sql = "INSERT INTO car_class (name, base_price) VALUES (?, ?)";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, carClass.getName());
                    preparedStatement.setBigDecimal(2, carClass.getBasePrice());
                    preparedStatement.executeUpdate();
                    return carClass;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving car class.", e);
        }
    }

    @Override
    public Optional<CarClass> findById(Long id) {
        String sql = "SELECT classes.name, classes.base_price FROM car_class AS classes WHERE classes.id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToCarClass(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding car class by id.", e);
        }
    }

    @Override
    public List<CarClass> findAll() {
        String sql = "SELECT classes.name, classes.base_price FROM car_class AS classes";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<CarClass> carClassList = new ArrayList<>();
                    while (resultSet.next()) {
                        carClassList.add(mapRowToCarClass(resultSet));
                    }
                    return carClassList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all car classes.", e);
        }
    }

    @Override
    public boolean update(Long id, CarClass carClass) {
        String sql = "UPDATE car_class SET name = ?, base_price = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, carClass.getName());
                    preparedStatement.setBigDecimal(2, carClass.getBasePrice());
                    preparedStatement.setLong(3, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating car class.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM car_class WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting car class.", e);
        }
    }

    private CarClass mapRowToCarClass(ResultSet resultSet) {
        try {
            return new CarClass(
                    resultSet.getString("name"),
                    resultSet.getBigDecimal("base_price")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping car class.", e);
        }
    }
}