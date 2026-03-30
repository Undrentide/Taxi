package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.Dao;
import ua.solvd.taxi.domain.dal.JdbcAware;
import ua.solvd.taxi.domain.exception.PersistenceException;
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
import java.util.UUID;

public class ReviewJdbcDao extends JdbcAware implements Dao<Review> {

    private static final class SqlQuery {
        static final String INSERT = "INSERT INTO review (id, order_id, rating, comment) VALUES (?, ?, ?, ?)";

        static final String UPDATE = "UPDATE review SET rating = ?, comment = ? WHERE id = ?";

        static final String DELETE = "DELETE FROM review WHERE id = ?";

        static final String BASE_SELECT = """
                  SELECT
                      reviews.id AS review_id, reviews.rating AS review_rating, reviews.comment AS review_comment,
                      orders.id AS order_id, orders.from_address, orders.to_address, orders.created_at AS order_date,
                      clients.id AS client_id, clients.first_name AS client_fn, clients.last_name AS client_ln, clients.phone AS client_ph,
                      client_roles.id AS client_role_id, client_roles.name AS client_role,
                      drivers.id AS driver_id, drivers.rating AS drv_rating,
                      drv_users.id AS drv_user_id, drv_users.first_name AS drv_fn, drv_users.last_name AS drv_ln, drv_users.phone AS drv_ph,
                      drv_roles.id AS drv_role_id, drv_roles.name AS drv_role,
                      cars.id AS car_id, cars.brand, cars.model, cars.license_plate, cars.color,
                      classes.id AS class_id, classes.name AS class_name, classes.base_price AS class_price,
                      drv_statuses.id AS drv_status_id, drv_statuses.name AS drv_status_name,
                      ord_statuses.id AS order_status_id, ord_statuses.name AS order_status_name,
                      promos.id AS promo_id, promos.code AS promo_code, promos.discount_percent, promos.is_active,
                      regions.id AS region_id, regions.name AS region_name, regions.multiplier AS region_multiplier
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

        static final String FIND_BY_ID = BASE_SELECT + " WHERE reviews.id = ?";
    }

    @Override
    public Review save(Review review) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.INSERT)) {
                preparedStatement.setString(1, review.getId().toString());
                preparedStatement.setString(2, review.getOrder().getId().toString());
                preparedStatement.setInt(3, review.getRating());
                preparedStatement.setString(4, review.getComment());
                preparedStatement.executeUpdate();
                return review;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while saving review.", e);
            }
        });
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_ID)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToReview(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding review by id.", e);
            }
        });
    }

    @Override
    public List<Review> findAll() {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.BASE_SELECT);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                List<Review> reviewList = new ArrayList<>();
                while (resultSet.next()) {
                    reviewList.add(mapRowToReview(resultSet));
                }
                return reviewList;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding all reviews.", e);
            }
        });
    }

    @Override
    public boolean update(Review review) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.UPDATE)) {
                preparedStatement.setInt(1, review.getRating());
                preparedStatement.setString(2, review.getComment());
                preparedStatement.setString(3, review.getId().toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while updating review.", e);
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
                throw new PersistenceException("Error occurred while deleting review.", e);
            }
        });
    }

    private Review mapRowToReview(ResultSet resultSet) throws SQLException {
        Role clientRole = new Role(
                UUID.fromString(resultSet.getString("client_role_id")),
                resultSet.getString("client_role")
        );
        User client = new User(
                UUID.fromString(resultSet.getString("client_id")),
                resultSet.getString("client_fn"),
                resultSet.getString("client_ln"),
                resultSet.getString("client_ph"),
                clientRole
        );
        Role drvRole = new Role(
                UUID.fromString(resultSet.getString("drv_role_id")),
                resultSet.getString("drv_role")
        );
        User driverUser = new User(
                UUID.fromString(resultSet.getString("drv_user_id")),
                resultSet.getString("drv_fn"),
                resultSet.getString("drv_ln"),
                resultSet.getString("drv_ph"),
                drvRole
        );
        CarClass carClass = new CarClass(
                UUID.fromString(resultSet.getString("class_id")),
                resultSet.getString("class_name"),
                resultSet.getBigDecimal("class_price")
        );
        Car car = new Car(
                UUID.fromString(resultSet.getString("car_id")),
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("license_plate"),
                resultSet.getString("color"),
                carClass
        );
        DriverStatus driverStatus = new DriverStatus(
                UUID.fromString(resultSet.getString("drv_status_id")),
                resultSet.getString("drv_status_name")
        );
        Driver driver = new Driver(
                UUID.fromString(resultSet.getString("driver_id")),
                driverUser,
                car,
                driverStatus,
                resultSet.getBigDecimal("drv_rating")
        );
        OrderStatus orderStatus = new OrderStatus(
                UUID.fromString(resultSet.getString("order_status_id")),
                resultSet.getString("order_status_name")
        );
        PromoCode promoCode = null;
        if (resultSet.getString("promo_id") != null) {
            promoCode = new PromoCode(
                    UUID.fromString(resultSet.getString("promo_id")),
                    resultSet.getString("promo_code"),
                    resultSet.getInt("discount_percent"),
                    resultSet.getBoolean("is_active")
            );
        }
        Region region = new Region(
                UUID.fromString(resultSet.getString("region_id")),
                resultSet.getString("region_name"),
                resultSet.getBigDecimal("region_multiplier")
        );
        Order order = new Order(
                UUID.fromString(resultSet.getString("order_id")),
                client,
                driver,
                orderStatus,
                promoCode,
                region,
                resultSet.getString("from_address"),
                resultSet.getString("to_address"),
                resultSet.getTimestamp("order_date").toInstant()
        );
        return new Review(
                UUID.fromString(resultSet.getString("review_id")),
                order,
                resultSet.getInt("review_rating"),
                resultSet.getString("review_comment")
        );
    }
}