package ua.solvd.taxi.domain.dal.jdbcimpl;

import ua.solvd.taxi.domain.dal.JdbcDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.OrderStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderStatusJdbcDao extends JdbcDao<OrderStatus> {

    private static final class SqlQuery {
        static final String INSERT = "INSERT INTO order_status (id, name) VALUES (?, ?)";

        static final String UPDATE = "UPDATE order_status SET name = ? WHERE id = ?";

        static final String DELETE = "DELETE FROM order_status WHERE id = ?";

        static final String BASE_SELECT = "SELECT status.id, status.name FROM order_status AS status";

        static final String FIND_BY_ID = BASE_SELECT + " WHERE status.id = ?";

        static final String FIND_BY_NAME = BASE_SELECT + " WHERE status.name = ?";
    }

    @Override
    public OrderStatus save(OrderStatus orderStatus) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.INSERT)) {
                preparedStatement.setString(1, orderStatus.getId().toString());
                preparedStatement.setString(2, orderStatus.getName());
                preparedStatement.executeUpdate();
                return orderStatus;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while saving order status.", e);
            }
        });
    }

    @Override
    public Optional<OrderStatus> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_ID)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToOrderStatus(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding order status by id.", e);
            }
        });

    }

    @Override
    public List<OrderStatus> findAll() {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.BASE_SELECT);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                List<OrderStatus> orderStatusList = new ArrayList<>();
                while (resultSet.next()) {
                    orderStatusList.add(mapRowToOrderStatus(resultSet));
                }
                return orderStatusList;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding all order statuses.", e);
            }
        });
    }

    public Optional<OrderStatus> findByName(String name) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_NAME)) {
                preparedStatement.setString(1, name);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToOrderStatus(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding order status by name.", e);
            }
        });
    }

    @Override
    public boolean update(OrderStatus orderStatus) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.UPDATE)) {
                preparedStatement.setString(1, orderStatus.getName());
                preparedStatement.setString(2, orderStatus.getId().toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while updating order status.", e);
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
                throw new PersistenceException("Error occurred while deleting order status.", e);
            }
        });
    }

    private OrderStatus mapRowToOrderStatus(ResultSet resultSet) throws SQLException {
        return new OrderStatus(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("name")
        );
    }
}