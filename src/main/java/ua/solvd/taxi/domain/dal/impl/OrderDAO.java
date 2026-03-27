package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.DriverStatus;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.OrderStatus;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderDAO implements DAO<Order> {

    @Override
    public Order save(Order order) {
        String insertSql = """
                  INSERT INTO `order` (id, client_id, driver_id, status_id, promo_code_id, region_id, from_address, to_address, created_at)
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                    preparedStatement.setString(1, order.getId().toString());
                    preparedStatement.setString(2, order.getClient().getId().toString());
                    preparedStatement.setString(3, order.getDriver().getId().toString());
                    preparedStatement.setString(4, order.getOrderStatus().getId().toString());
                    if (order.getPromoCode() != null) {
                        preparedStatement.setString(5, order.getPromoCode().getId().toString());
                    } else {
                        preparedStatement.setNull(5, Types.VARCHAR);
                    }
                    preparedStatement.setString(6, order.getRegion().getId().toString());
                    preparedStatement.setString(7, order.getFromAddress());
                    preparedStatement.setString(8, order.getToAddress());
                    preparedStatement.setTimestamp(9, Timestamp.from(order.getCreatedAt()));
                    preparedStatement.executeUpdate();
                    return order;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving order.", e);
        }
    }

    @Override
    public Optional<Order> findById(UUID id) {
        String sql = getBaseSelectQuery() + " WHERE orders.id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return Optional.of(mapRowToOrder(resultSet));
                        }
                        return Optional.empty();
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding order by id.", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String sql = getBaseSelectQuery();
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        List<Order> orderList = new ArrayList<>();
                        while (resultSet.next()) {
                            orderList.add(mapRowToOrder(resultSet));
                        }
                        return orderList;
                    }
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all orders.", e);
        }
    }

    @Override
    public boolean update(Order order) {
        String sql = "UPDATE `order` SET status_id = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, order.getOrderStatus().getId().toString());
                    preparedStatement.setString(2, order.getId().toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating order.", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM `order` WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting order.", e);
        }
    }

    private String getBaseSelectQuery() {
        return """
                  SELECT
                      orders.id AS order_id, orders.from_address, orders.to_address, orders.created_at AS order_date,
                      clients.id AS client_id, clients.first_name AS client_fn, clients.last_name AS client_ln, clients.phone AS client_ph,
                      client_roles.id AS client_role_id, client_roles.name AS client_role,
                      drivers.id AS driver_id, drivers.rating AS driver_rating,
                      drv_users.id AS drv_user_id, drv_users.first_name AS drv_fn, drv_users.last_name AS drv_ln, drv_users.phone AS drv_ph,
                      drv_roles.id AS drv_role_id, drv_roles.name AS drv_role,
                      cars.id AS car_id, cars.brand, cars.model, cars.license_plate, cars.color,
                      classes.id AS class_id, classes.name AS class_name, classes.base_price AS class_price,
                      drv_statuses.id AS drv_status_id, drv_statuses.name AS drv_status_name,
                      ord_statuses.id AS order_status_id, ord_statuses.name AS order_status_name,
                      promos.id AS promo_id, promos.code AS promo_code, promos.discount_percent, promos.is_active,
                      regions.id AS region_id, regions.name AS region_name, regions.multiplier AS region_multiplier
                  FROM `order` AS orders
                  INNER JOIN user AS clients ON orders.client_id = clients.id
                  INNER JOIN role AS client_roles ON clients.role_id = client_roles.id
                  INNER JOIN driver AS drivers ON orders.driver_id = drivers.id
                  INNER JOIN user AS drv_users ON drivers.user_id = drv_users.id
                  INNER JOIN role AS drv_roles ON drv_users.role_id = drv_roles.id
                  INNER JOIN car AS cars ON drivers.car_id = cars.id
                  INNER JOIN car_class AS classes ON cars.class_id = classes.id
                  INNER JOIN driver_status AS drv_statuses ON drivers.status_id = drv_statuses.id
                  INNER JOIN order_status AS ord_statuses ON orders.status_id = ord_statuses.id
                  LEFT JOIN promo_code AS promos ON orders.promo_code_id = promos.id
                  INNER JOIN region AS regions ON orders.region_id = regions.id
                """;
    }

    private Order mapRowToOrder(ResultSet resultSet) throws SQLException {
        Role clientRole = new Role(
                UUID.fromString(
                        resultSet.getString("client_role_id")),
                resultSet.getString("client_role"));
        User client = new User(
                UUID.fromString(
                        resultSet.getString("client_id")),
                resultSet.getString("client_fn"),
                resultSet.getString("client_ln"),
                resultSet.getString("client_ph"),
                clientRole);
        Role drvRole = new Role(
                UUID.fromString(
                        resultSet.getString("drv_role_id")),
                resultSet.getString("drv_role"));
        User driverUser = new User(
                UUID.fromString(
                        resultSet.getString("drv_user_id")),
                resultSet.getString("drv_fn"),
                resultSet.getString("drv_ln"),
                resultSet.getString("drv_ph"),
                drvRole);
        CarClass carClass = new CarClass(
                UUID.fromString(
                        resultSet.getString("class_id")),
                resultSet.getString("class_name"),
                resultSet.getBigDecimal("class_price"));
        Car car = new Car(
                UUID.fromString(
                        resultSet.getString("car_id")),
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                carClass);
        DriverStatus drvStatus = new DriverStatus(
                UUID.fromString(
                        resultSet.getString("drv_status_id")),
                resultSet.getString("drv_status_name"));
        Driver driver = new Driver(
                UUID.fromString(
                        resultSet.getString("driver_id")),
                driverUser,
                car,
                drvStatus,
                resultSet.getBigDecimal("driver_rating"));
        OrderStatus orderStatus = new OrderStatus(
                UUID.fromString(
                        resultSet.getString("order_status_id")),
                resultSet.getString("order_status_name"));
        PromoCode promoCode = null;
        if (resultSet.getString("promo_id") != null) {
            promoCode = new PromoCode(UUID.fromString(
                    resultSet.getString("promo_id")),
                    resultSet.getString("promo_code"),
                    resultSet.getInt("discount_percent"),
                    resultSet.getBoolean("is_active"));
        }
        Region region = new Region(
                UUID.fromString(
                        resultSet.getString("region_id")),
                resultSet.getString("region_name"),
                resultSet.getBigDecimal("region_multiplier"));
        return new Order(
                UUID.fromString(
                        resultSet.getString("order_id")),
                client,
                driver,
                orderStatus,
                promoCode,
                region,
                resultSet.getString("from_address"),
                resultSet.getString("to_address"),
                resultSet.getTimestamp("order_date").toInstant()
        );
    }
}