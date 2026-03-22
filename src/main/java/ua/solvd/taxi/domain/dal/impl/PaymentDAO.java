package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.CarClass;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.DriverStatus;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.OrderStatus;
import ua.solvd.taxi.domain.model.impl.Payment;
import ua.solvd.taxi.domain.model.impl.PaymentType;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO extends AbstractDAO implements DAO<Long, Payment> {

    @Override
    public Payment save(Payment payment) throws SQLException {
        String findIdsSql = """
                 SELECT
                     orders.id AS target_order_id,
                     types.id AS target_type_id
                 FROM `order` AS orders
                 JOIN payment_type AS types ON types.name = ?
                 WHERE orders.id = ?
                """;
        String insertSql = """
                 INSERT INTO payment (order_id, amount, payment_type_id, paid_at)
                 VALUES (?, ?, ?, ?)
                """;
        return execute(connection -> {
            long orderId, typeId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findIdsSql)) {
                preparedStatement.setString(1, payment.getPaymentType().getName());
                preparedStatement.setObject(2, payment.getOrder().getUuid());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        orderId = resultSet.getLong("target_order_id");
                        typeId = resultSet.getLong("target_type_id");

                        if (orderId == 0)
                            throw new SQLException("Order not found for UUID: " + payment.getOrder().getUuid());
                        if (typeId == 0)
                            throw new SQLException("PaymentType not found: " + payment.getPaymentType().getName());
                    } else {
                        throw new SQLException("Failed to retrieve IDs: Order or PaymentType is missing.");
                    }
                }
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                preparedStatement.setLong(1, orderId);
                preparedStatement.setBigDecimal(2, payment.getAmount());
                preparedStatement.setLong(3, typeId);
                preparedStatement.setTimestamp(4, Timestamp.from(payment.getPaidAt()));
                preparedStatement.executeUpdate();
                return payment;
            }
        });
    }

    @Override
    public Optional<Payment> findById(Long id) throws SQLException {
        String sql = getBaseSelectQuery() + " WHERE pay.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToPayment(resultSet));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        String sql = getBaseSelectQuery();
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<Payment> paymentList = new ArrayList<>();
                    while (resultSet.next()) {
                        paymentList.add(mapRowToPayment(resultSet));
                    }
                    return paymentList;
                }
            }
        });
    }

    @Override
    public boolean update(Long id, Payment payment) throws SQLException {
        String sql = "UPDATE payment SET amount = ?, paid_at = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setBigDecimal(1, payment.getAmount());
                preparedStatement.setTimestamp(2, Timestamp.from(payment.getPaidAt()));
                preparedStatement.setLong(3, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM payment WHERE id = ?";
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
                      pay.amount AS payment_amount, pay.paid_at AS payment_date,
                      pt.name AS payment_type_name,
                      ord.from_address, ord.to_address, ord.created_at AS order_date,
                      cl.first_name AS client_fn, cl.last_name AS client_ln, cl.phone AS client_ph,
                      cl_r.name AS client_role_name,
                      dr.rating AS driver_rating,
                      dr_u.first_name AS driver_fn, dr_u.last_name AS driver_ln, dr_u.phone AS driver_ph,
                      dr_r.name AS driver_role_name,
                      car.brand AS car_brand, car.model AS car_model, car.license_plate AS car_plate, car.color AS car_color,
                      car_cl.name AS car_class_name, car_cl.base_price AS car_class_price,
                      dr_st.name AS driver_status_name,
                      ord_st.name AS order_status_name,
                      p_code.code AS promo_code, p_code.discount_percent AS promo_discount, p_code.is_active AS promo_active,
                      reg.name AS region_name, reg.multiplier AS region_multiplier
                  FROM payment AS pay
                  INNER JOIN payment_type AS pt ON pay.payment_type_id = pt.id
                  INNER JOIN `order` AS ord ON pay.order_id = ord.id
                  INNER JOIN user AS cl ON ord.client_id = cl.id
                  INNER JOIN role AS cl_r ON cl.role_id = cl_r.id
                  INNER JOIN driver AS dr ON ord.driver_id = dr.id
                  INNER JOIN user AS du ON dr.user_id = du.id
                  INNER JOIN role AS dr_r ON du.role_id = dr_r.id
                  INNER JOIN car AS car ON dr.car_id = car.id
                  INNER JOIN car_class AS car_cl ON car.class_id = car_cl.id
                  INNER JOIN driver_status AS dr_st ON dr.status_id = dr_st.id
                  INNER JOIN order_status AS ord_st ON ord.status_id = ord_st.id
                  LEFT JOIN promo_code AS p_code ON ord.promo_code_id = p_code.id
                  INNER JOIN region AS reg ON ord.region_id = reg.id
                """;
    }

    private Payment mapRowToPayment(ResultSet resultSet) throws SQLException {
        User client = new User(
                resultSet.getString("client_fn"),
                resultSet.getString("client_ln"),
                resultSet.getString("client_ph"),
                new Role(resultSet.getString("client_role_name"))
        );
        User driverUser = new User(
                resultSet.getString("driver_fn"),
                resultSet.getString("driver_ln"),
                resultSet.getString("driver_ph"),
                new Role(resultSet.getString("driver_role_name"))
        );
        Car car = new Car(
                resultSet.getString("car_brand"),
                resultSet.getString("car_model"),
                resultSet.getString("car_plate"),
                resultSet.getString("car_color"),
                new CarClass(resultSet.getString("car_class_name"), resultSet.getBigDecimal("car_class_price"))
        );
        Driver driver = new Driver(
                driverUser, car,
                new DriverStatus(resultSet.getString("driver_status_name")),
                resultSet.getBigDecimal("driver_rating")
        );
        PromoCode promo = null;
        String promoCode = resultSet.getString("promo_code");
        if (promoCode != null) {
            promo = new PromoCode(promoCode, resultSet.getInt("promo_discount"), resultSet.getBoolean("promo_active"));
        }
        Order order = new Order(
                client, driver,
                new OrderStatus(resultSet.getString("order_status_name")),
                promo,
                new Region(resultSet.getString("region_name"), resultSet.getBigDecimal("region_multiplier")),
                resultSet.getString("from_address"),
                resultSet.getString("to_address"),
                resultSet.getTimestamp("order_date").toInstant()
        );
        return new Payment(
                order,
                resultSet.getBigDecimal("payment_amount"),
                new PaymentType(resultSet.getString("payment_type_name")),
                resultSet.getTimestamp("payment_date").toInstant()
        );
    }
}