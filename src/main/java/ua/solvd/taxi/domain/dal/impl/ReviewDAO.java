package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.DriverStatus;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.OrderStatus;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.Review;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDAO extends AbstractDAO implements DAO<Long, Review> {

    @Override
    public Review save(Review review) throws SQLException {
        String findOrderIdSql = "SELECT ord.id AS target_order_id FROM `order` AS ord WHERE ord.id = ?";
        String insertSql = "INSERT INTO review (order_id, rating, comment) VALUES (?, ?, ?)";
        return execute(connection -> {
            long orderId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findOrderIdSql)) {
                preparedStatement.setObject(1, review.getOrder().getUuid());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        orderId = resultSet.getLong("target_order_id");
                    } else {
                        throw new SQLException("Review failed: Order with UUID " + review.getOrder().getUuid() + " not found.");
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
                  SELECT
                      reviews.rating AS review_rating, reviews.comment AS review_comment,
                      orders.from_address, orders.to_address, orders.created_at AS order_date,
                      clients.first_name AS client_fn, clients.last_name AS client_ln, clients.phone AS client_ph,
                      client_roles.name AS client_role,
                      drivers.rating AS drv_rating,
                      drv_users.first_name AS drv_fn, drv_users.last_name AS drv_ln, drv_users.phone AS drv_ph,
                      drv_roles.name AS drv_role,
                      cars.brand, cars.model, cars.license_plate, cars.color,
                      classes.name AS class_name, classes.base_price AS class_price,
                      drv_statuses.name AS drv_status_name,
                      ord_statuses.name AS order_status_name,
                      promos.code AS promo_code, promos.discount_percent, promos.is_active,
                      regions.name AS region_name, regions.multiplier AS region_multiplier
                  FROM review AS reviews
                  INNER JOIN `order` AS orders ON reviews.order_id = orders.id
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

    private Review mapRowToReview(ResultSet resultSet) throws SQLException {
        User client = new User(
                resultSet.getString("client_fn"),
                resultSet.getString("client_ln"),
                resultSet.getString("client_ph"),
                new Role(resultSet.getString("client_role"))
        );
        User drvUser = new User(
                resultSet.getString("drv_fn"),
                resultSet.getString("drv_ln"),
                resultSet.getString("drv_ph"),
                new Role(resultSet.getString("drv_role"))
        );
        CarClass carCl = new CarClass(resultSet.getString("class_name"), resultSet.getBigDecimal("class_price"));
        Car car = new Car(resultSet.getString("brand"), resultSet.getString("model"),
                resultSet.getString("license_plate"), resultSet.getString("color"), carCl);
        Driver driver = new Driver(drvUser, car, new DriverStatus(resultSet.getString("drv_status_name")),
                resultSet.getBigDecimal("drv_rating"));
        PromoCode promo = null;
        String promoCode = resultSet.getString("promo_code");
        if (promoCode != null) {
            promo = new PromoCode(promoCode, resultSet.getInt("discount_percent"), resultSet.getBoolean("is_active"));
        }
        Order order = new Order(
                client,
                driver,
                new OrderStatus(resultSet.getString("order_status_name")),
                promo,
                new Region(resultSet.getString("region_name"), resultSet.getBigDecimal("region_multiplier")),
                resultSet.getString("from_address"),
                resultSet.getString("to_address"),
                resultSet.getTimestamp("order_date").toInstant()
        );
        return new Review(
                order,
                resultSet.getInt("review_rating"),
                resultSet.getString("review_comment")
        );
    }
}