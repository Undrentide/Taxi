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
import ua.solvd.taxi.model.impl.Review;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDAO extends AbstractDAO implements DAO<Long, Review> {

    @Override
    public Review save(Review review) throws SQLException {
        String findOrderIdSql = "SELECT id FROM `order` WHERE created_at = ?";
        String insertSql = "INSERT INTO review (order_id, rating, comment) VALUES (?, ?, ?)";
        return execute(connection -> {
            long orderId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findOrderIdSql)) {
                preparedStatement.setTimestamp(1, Timestamp.from(review.getOrder().getCreatedAt()));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        orderId = resultSet.getLong("id");
                    } else {
                        throw new SQLException("Order not found for review at: " + review.getOrder().getCreatedAt());
                    }
                }
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                preparedStatement.setLong(1, orderId);
                preparedStatement.setInt(2, review.getRating());
                preparedStatement.setString(3, review.getComment());
                preparedStatement.executeUpdate();
                return review;
            }
        });
    }

    @Override
    public Optional<Review> findById(Long id) throws SQLException {
        String sql = getBaseSelectQuery() + " WHERE reviews.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToReview(resultSet));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Review> findAll() throws SQLException {
        String sql = getBaseSelectQuery();
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<Review> reviewList = new ArrayList<>();
                    while (resultSet.next()) {
                        reviewList.add(mapRowToReview(resultSet));
                    }
                    return reviewList;
                }
            }
        });
    }

    @Override
    public boolean update(Long id, Review review) throws SQLException {
        String sql = "UPDATE review SET rating = ?, comment = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, review.getRating());
                preparedStatement.setString(2, review.getComment());
                preparedStatement.setLong(3, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM review WHERE id = ?";
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
                     reviews.rating, reviews.comment,
                     orders.from_address, orders.to_address, orders.created_at AS order_date,
                     clients.first_name AS client_fn, clients.last_name AS client_ln, clients.phone AS client_ph,
                     client_roles.name AS client_role,
                     drivers.rating AS drv_rating,
                     drv_users.first_name AS drv_fn, drv_users.last_name AS drv_ln, drv_users.phone AS drv_ph,
                     drv_roles.name AS drv_role,
                     cars.brand, cars.model, cars.license_plate, cars.color,
                     classes.name AS class_name, classes.base_price AS class_price,
                     drv_statuses.name AS drv_status_name,
                     order_statuses.name AS order_status_name,
                     promos.code AS promo_code, promos.discount_percent, promos.is_active,
                     regions.name AS region_name, regions.multiplier AS region_multiplier
                 FROM review AS reviews
                 JOIN `order` AS orders ON reviews.order_id = orders.id
                 JOIN user AS clients ON orders.client_id = clients.id
                 JOIN role AS client_roles ON clients.role_id = client_roles.id
                 JOIN driver AS drivers ON orders.driver_id = drivers.id
                 JOIN user AS drv_users ON drivers.user_id = drv_users.id
                 JOIN role AS drv_roles ON drv_users.role_id = drv_roles.id
                 JOIN car AS cars ON drivers.car_id = cars.id
                 JOIN car_class AS classes ON cars.class_id = classes.id
                 JOIN driver_status AS drv_statuses ON drivers.driver_status_id = drv_statuses.id
                 JOIN order_status AS order_statuses ON orders.status_id = order_statuses.id
                 JOIN promo_code AS promos ON orders.promo_code_id = promos.id
                 JOIN region AS regions ON orders.region_id = regions.id
                \s""";
    }

    private Review mapRowToReview(ResultSet resultSet) throws SQLException {
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
        Driver driver = new Driver(
                driverUser, car,
                new DriverStatus(resultSet.getString("drv_status_name")),
                resultSet.getBigDecimal("drv_rating")
        );
        Order order = new Order(
                client, driver,
                new OrderStatus(resultSet.getString("order_status_name")),
                new PromoCode(resultSet.getString("promo_code"), resultSet.getInt("discount_percent"), resultSet.getBoolean("is_active")),
                new Region(resultSet.getString("region_name"), resultSet.getBigDecimal("region_multiplier")),
                resultSet.getString("from_address"),
                resultSet.getString("to_address"),
                resultSet.getTimestamp("order_date").toInstant()
        );
        return new Review(
                order,
                resultSet.getInt("rating"),
                resultSet.getString("comment")
        );
    }
}