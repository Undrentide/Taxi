package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.Car;
import ua.solvd.taxi.model.impl.CarClass;
import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.DriverStatus;
import ua.solvd.taxi.model.impl.Order;
import ua.solvd.taxi.model.impl.OrderStatus;
import ua.solvd.taxi.model.impl.Payment;
import ua.solvd.taxi.model.impl.PaymentType;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.model.impl.Region;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.User;

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
                 SELECT\s
                     (SELECT id FROM `order` WHERE created_at = ?) AS order_id,
                     (SELECT id FROM payment_type WHERE name = ?) AS type_id
                \s""";
        String insertSql = """
                 INSERT INTO payment (order_id, amount, payment_type_id, paid_at)\s
                 VALUES (?, ?, ?, ?)
                \s""";
        return execute(connection -> {
            long orderId, typeId;
            try (PreparedStatement preparedStatement = connection.prepareStatement(findIdsSql)) {
                preparedStatement.setTimestamp(1, Timestamp.from(payment.getOrder().getCreatedAt()));
                preparedStatement.setString(2, payment.getPaymentType().getName());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        orderId = resultSet.getLong("order_id");
                        typeId = resultSet.getLong("type_id");
                    } else {
                        throw new SQLException("Related Order or PaymentType not found");
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
                 SELECT\s
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
                 JOIN payment_type AS pt ON pay.payment_type_id = pt.id
                 JOIN `order` AS ord ON pay.order_id = ord.id
                 JOIN user AS cl ON ord.client_id = cl.id
                 JOIN role AS cl_r ON cl.role_id = cl_r.id
                 JOIN driver AS dr ON ord.driver_id = dr.id
                 JOIN user AS dr_u ON dr.user_id = dr_u.id
                 JOIN role AS dr_r ON dr_u.role_id = dr_r.id
                 JOIN car AS car ON dr.car_id = car.id
                 JOIN car_class AS car_cl ON car.class_id = car_cl.id
                 JOIN driver_status AS dr_st ON dr.driver_status_id = dr_st.id
                 JOIN order_status AS ord_st ON ord.status_id = ord_st.id
                 JOIN promo_code AS p_code ON ord.promo_code_id = p_code.id
                 JOIN region AS reg ON ord.region_id = reg.id
                \s""";
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
        Order order = new Order(
                client, driver,
                new OrderStatus(resultSet.getString("order_status_name")),
                new PromoCode(resultSet.getString("promo_code"), resultSet.getInt("promo_discount"), resultSet.getBoolean("promo_active")),
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