package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.OrderStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderStatusDAO extends AbstractDAO implements DAO<Long, OrderStatus> {

    @Override
    public OrderStatus save(OrderStatus orderStatus) throws SQLException {
        String sql = "INSERT INTO order_status (name) VALUES (?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, orderStatus.getName());
                preparedStatement.executeUpdate();
                return orderStatus;
            }
        });
    }

    @Override
    public Optional<OrderStatus> findById(Long id) throws SQLException {
        String sql = "SELECT status.name FROM order_status AS status WHERE status.id = ?";
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
    }

    @Override
    public List<OrderStatus> findAll() throws SQLException {
        String sql = "SELECT status.name FROM order_status AS status";
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
    }

    public Optional<OrderStatus> findByName(String name) throws SQLException {
        String sql = "SELECT status.name FROM order_status AS status WHERE status.name = ?";
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
    }

    @Override
    public boolean update(Long id, OrderStatus orderStatus) throws SQLException {
        String sql = "UPDATE order_status SET name = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, orderStatus.getName());
                preparedStatement.setLong(2, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM order_status WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private OrderStatus mapRowToOrderStatus(ResultSet resultSet) throws SQLException {
        return new OrderStatus(resultSet.getString("name"));
    }
}