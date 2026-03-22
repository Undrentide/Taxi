package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Car;
import ua.solvd.taxi.model.impl.CarClass;
import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.DriverStatus;
import ua.solvd.taxi.model.impl.Order;
import ua.solvd.taxi.model.impl.OrderStatus;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.model.impl.Region;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO extends AbstractDAO implements DAO<Long, Order> {

    @Override
    public Order save(Order order) throws SQLException {
        String findIdsSql = """
                  SELECT\s
                      (SELECT users.id FROM user AS users WHERE users.phone = ?) AS client_id,
                      (SELECT drivers.id FROM driver AS drivers\s
                       JOIN user AS driver_users ON drivers.user_id = driver_users.id\s
                       WHERE driver_users.phone = ?) AS driver_id,
                      (SELECT order_statuses.id FROM order_status AS order_statuses WHERE order_statuses.name = ?) AS status_id,
                      (SELECT promo_codes.id FROM promo_code AS promo_codes WHERE promo_codes.code = ?) AS promo_id,
                      (SELECT regions.id FROM region AS regions WHERE regions.name = ?) AS region_id
                \s""";
        String insertSql = """
                  INSERT INTO `order` (client_id, driver_id, status_id, promo_code_id, region_id, from_address, to_address, created_at)\s
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                \s""";
        return execute(connection -> {
            long clientId, driverId, statusId, regionId;
            Long promoId = null;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findIdsSql)) {
                preparedStatement.setString(1, order.getClient().getPhone());
                preparedStatement.setString(2, order.getDriver().getUser().getPhone());
                preparedStatement.setString(3, order.getOrderStatus().getName());
                if (order.getPromoCode() != null) {
                    preparedStatement.setString(4, order.getPromoCode().getCode());
                } else {
                    preparedStatement.setNull(4, Types.VARCHAR);
                }
                preparedStatement.setString(5, order.getRegion().getName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        clientId = resultSet.getLong("client_id");
                        driverId = resultSet.getLong("driver_id");
                        statusId = resultSet.getLong("status_id");
                        regionId = resultSet.getLong("region_id");
                        long id = resultSet.getLong("promo_id");
                        if (!resultSet.wasNull()) {
                            promoId = id;
                        }
                        if (clientId == 0 || driverId == 0 || statusId == 0 || regionId == 0) {
                            throw new SQLException("Required related entities (Client, Driver, Status or Region) not found.");
                        }
                    } else {
                        throw new SQLException("Failed to retrieve entity IDs.");
                    }
                }
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                preparedStatement.setLong(1, clientId);
                preparedStatement.setLong(2, driverId);
                preparedStatement.setLong(3, statusId);
                if (promoId != null) {
                    preparedStatement.setLong(4, promoId);
                } else {
                    preparedStatement.setNull(4, Types.BIGINT);
                }
                preparedStatement.setLong(5, regionId);
                preparedStatement.setString(6, order.getFromAddress());
                preparedStatement.setString(7, order.getToAddress());
                preparedStatement.setTimestamp(8, Timestamp.from(order.getCreatedAt()));
                preparedStatement.executeUpdate();
                return order;
            }
        });
    }

    @Override
    public Optional<Order> findById(Long id) throws SQLException {
        String sql = getBaseSelectQuery() + " WHERE orders.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToOrder(resultSet));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Order> findAll() throws SQLException {
        String sql = getBaseSelectQuery();
        return execute(connection -> {
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
    }

    @Override
    public boolean update(Long id, Order order) throws SQLException {
        String sql = "UPDATE `order` SET status_id = (SELECT id FROM order_status WHERE name = ?) WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, order.getOrderStatus().getName());
                preparedStatement.setLong(2, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM `order` WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private String getBaseSelectQuery() {
        return """
                 SELECT\s
                     orders.from_address, orders.to_address, orders.created_at,
                     clients.first_name AS client_fn, clients.last_name AS client_ln, clients.phone AS client_ph,
                     client_roles.name AS client_role,
                     drivers.rating AS driver_rating,
                     driver_users.first_name AS drv_fn, driver_users.last_name AS drv_ln, driver_users.phone AS drv_ph,
                     driver_roles.name AS drv_role,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     drv_statuses.name AS drv_status_name,
                     statuses.name AS order_status_name,
                     promos.code AS promo_code, promos.discount_percent, promos.is_active,
                     regions.name AS region_name, regions.multiplier AS region_multiplier
                 FROM `order` AS orders
                 JOIN user AS clients ON orders.client_id = clients.id
                 JOIN role AS client_roles ON clients.role_id = client_roles.id
                 JOIN driver AS drivers ON orders.driver_id = drivers.id
                 JOIN user AS driver_users ON drivers.user_id = driver_users.id
                 JOIN role AS driver_roles ON driver_users.role_id = driver_roles.id
                 JOIN car AS cars ON drivers.car_id = cars.id
                 JOIN car_class AS classes ON cars.class_id = classes.id
                 JOIN driver_status AS drv_statuses ON drivers.driver_status_id = drv_statuses.id
                 JOIN order_status AS statuses ON orders.status_id = statuses.id
                 JOIN promo_code AS promos ON orders.promo_code_id = promos.id
                 JOIN region AS regions ON orders.region_id = regions.id
                \s""";
    }

    private Order mapRowToOrder(ResultSet resultSet) throws SQLException {
        User client = new User(
                resultSet.getString("client_fn"),
                resultSet.getString("client_ln"),
                resultSet.getString("client_ph"),
                new Role(resultSet.getString("client_role"))
        );
        User driverUser = new User(
                resultSet.getString("drv_fn"),
                resultSet.getString("drv_ln"),
                resultSet.getString("drv_ph"),
                new Role(resultSet.getString("drv_role"))
        );
        Car car = new Car(
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                new CarClass(resultSet.getString("class_name"), resultSet.getBigDecimal("class_price"))
        );
        Driver driver = new Driver(driverUser, car, new DriverStatus(resultSet.getString("drv_status_name")), resultSet.getBigDecimal("driver_rating"));
        OrderStatus orderStatus = new OrderStatus(resultSet.getString("order_status_name"));
        PromoCode promoCode = new PromoCode(resultSet.getString("promo_code"), resultSet.getInt("discount_percent"), resultSet.getBoolean("is_active"));
        Region region = new Region(resultSet.getString("region_name"), resultSet.getBigDecimal("region_multiplier"));
        return new Order(
                client, driver, orderStatus, promoCode, region,
                resultSet.getString("from_address"),
                resultSet.getString("to_address"),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }
}