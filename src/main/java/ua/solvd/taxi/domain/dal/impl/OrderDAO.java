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

public class OrderDAO implements DAO<Long, Order> {

    @Override
    public Order save(Order order) {
        String findIdsSql = """
                 SELECT
                     u.id AS client_id,\s
                     d.id AS driver_id,\s
                     os.id AS status_id,\s
                     pc.id AS promo_id,\s
                     reg.id AS region_id
                 FROM user AS u
                 INNER JOIN driver AS d ON d.user_id = (SELECT id FROM user WHERE phone = ?)
                 INNER JOIN order_status AS os ON os.name = ?
                 INNER JOIN region AS reg ON reg.name = ?
                 LEFT JOIN promo_code AS pc ON pc.code = ?
                 WHERE u.phone = ?
                """;
        String insertSql = """
                  INSERT INTO `order` (client_id, driver_id, status_id, promo_code_id, region_id, from_address, to_address, created_at)
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            return DAOUtil.execute(connection -> {
                long clientId, driverId, statusId, regionId;
                Long promoId = null;

                try (PreparedStatement preparedStatement = connection.prepareStatement(findIdsSql)) {
                    preparedStatement.setString(1, order.getDriver().getUser().getPhone());
                    preparedStatement.setString(2, order.getOrderStatus().getName());
                    preparedStatement.setString(3, order.getRegion().getName());
                    if (order.getPromoCode() != null) {
                        preparedStatement.setString(4, order.getPromoCode().getCode());
                    } else {
                        preparedStatement.setNull(4, Types.VARCHAR);
                    }
                    preparedStatement.setString(5, order.getClient().getPhone());
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
                                throw new SQLException("Required related entities not found (Client, Driver, Status or Region)");
                            }
                        } else {
                            throw new SQLException("Order lookup failed: Client or Driver not found.");
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
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving order.", e);
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = getBaseSelectQuery() + " WHERE orders.id = ?";
        try {
            return DAOUtil.execute(connection -> {
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
    public boolean update(Long id, Order order) {
        String sql = "UPDATE `order` SET status_id = (SELECT id FROM order_status WHERE name = ?) WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, order.getOrderStatus().getName());
                    preparedStatement.setLong(2, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating order.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM `order` WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
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
                      orders.from_address, orders.to_address, orders.created_at,
                      clients.first_name AS client_fn, clients.last_name AS client_ln, clients.phone AS client_ph,
                      client_roles.name AS client_role,
                      drivers.rating AS driver_rating,
                      driver_users.first_name AS drv_fn, driver_users.last_name AS drv_ln, driver_users.phone AS drv_ph,
                      driver_roles.name AS drv_role,
                      cars.brand, cars.model, cars.license_plate, cars.color,
                      classes.name AS class_name, classes.base_price AS class_price,
                      drv_statuses.name AS drv_status_name,
                      ord_statuses.name AS order_status_name,
                      promos.code AS promo_code, promos.discount_percent, promos.is_active,
                      regions.name AS region_name, regions.multiplier AS region_multiplier
                  FROM `order` AS orders
                  INNER JOIN user AS clients ON orders.client_id = clients.id
                  INNER JOIN role AS client_roles ON clients.role_id = client_roles.id
                  INNER JOIN driver AS drivers ON orders.driver_id = drivers.id
                  INNER JOIN user AS driver_users ON drivers.user_id = driver_users.id
                  INNER JOIN role AS driver_roles ON driver_users.role_id = driver_roles.id
                  INNER JOIN car AS cars ON drivers.car_id = cars.id
                  INNER JOIN car_class AS classes ON cars.class_id = classes.id
                  INNER JOIN driver_status AS drv_statuses ON drivers.status_id = drv_statuses.id
                  INNER JOIN order_status AS ord_statuses ON orders.status_id = ord_statuses.id
                  INNER JOIN promo_code AS promos ON orders.promo_code_id = promos.id
                  INNER JOIN region AS regions ON orders.region_id = regions.id
                """;
    }

    private Order mapRowToOrder(ResultSet resultSet) {
        Role clientRole;
        try {
            clientRole = new Role(resultSet.getString("client_role"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        User client;
        try {
            client = new User(
                    resultSet.getString("client_fn"),
                    resultSet.getString("client_ln"),
                    resultSet.getString("client_ph"),
                    clientRole
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        Role driverRole;
        try {
            driverRole = new Role(resultSet.getString("drv_role"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        User driverUser;
        try {
            driverUser = new User(
                    resultSet.getString("drv_fn"),
                    resultSet.getString("drv_ln"),
                    resultSet.getString("drv_ph"),
                    driverRole
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        CarClass carClass;
        try {
            carClass = new CarClass(
                    resultSet.getString("class_name"),
                    resultSet.getBigDecimal("class_price")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        Car car;
        try {
            car = new Car(
                    resultSet.getString("brand"),
                    resultSet.getString("model"),
                    resultSet.getString("license_plate"),
                    resultSet.getString("color"),
                    carClass
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        DriverStatus driverStatus;
        try {
            driverStatus = new DriverStatus(resultSet.getString("drv_status_name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        Driver driver;
        try {
            driver = new Driver(
                    driverUser,
                    car,
                    driverStatus,
                    resultSet.getBigDecimal("driver_rating")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        OrderStatus orderStatus;
        try {
            orderStatus = new OrderStatus(resultSet.getString("order_status_name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        String promoCode;
        try {
            promoCode = resultSet.getString("promo_code");
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        PromoCode promo = null;
        if (promoCode != null) {
            try {
                promo = new PromoCode(
                        promoCode,
                        resultSet.getInt("discount_percent"),
                        resultSet.getBoolean("is_active")
                );
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while mapping order.", e);
            }
        }
        Region region;
        try {
            region = new Region(
                    resultSet.getString("region_name"),
                    resultSet.getBigDecimal("region_multiplier")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
        try {
            return new Order(
                    client,
                    driver,
                    orderStatus,
                    promo,
                    region,
                    resultSet.getString("from_address"),
                    resultSet.getString("to_address"),
                    resultSet.getTimestamp("created_at").toInstant()
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping order.", e);
        }
    }
}