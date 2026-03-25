package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.OrderStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderStatusDAO extends AbstractDAO implements DAO<Long, OrderStatus> {

    @Override
    public OrderStatus save(OrderStatus orderStatus) {
        String sql = "INSERT INTO order_status (name) VALUES (?)";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, orderStatus.getName());
                    preparedStatement.executeUpdate();
                    return orderStatus;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving order status.", e);
        }
    }

    @Override
    public Optional<OrderStatus> findById(Long id) {
        String sql = "SELECT status.name FROM order_status AS status WHERE status.id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToOrderStatus(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding order status by id.", e);
        }
    }

    @Override
    public List<OrderStatus> findAll() {
        String sql = "SELECT status.name FROM order_status AS status";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<OrderStatus> orderStatusList = new ArrayList<>();
                    while (resultSet.next()) {
                        orderStatusList.add(mapRowToOrderStatus(resultSet));
                    }
                    return orderStatusList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all order statuses.", e);
        }
    }

    public Optional<OrderStatus> findByName(String name) {
        String sql = "SELECT status.name FROM order_status AS status WHERE status.name = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, name);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToOrderStatus(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding order status by name.", e);
        }
    }

    @Override
    public boolean update(Long id, OrderStatus orderStatus) {
        String sql = "UPDATE order_status SET name = ? WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, orderStatus.getName());
                    preparedStatement.setLong(2, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating order status.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM order_status WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting order status.", e);
        }
    }

    private OrderStatus mapRowToOrderStatus(ResultSet resultSet) {
        try {
            return new OrderStatus(resultSet.getString("name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order status.", e);
        }
    }
}