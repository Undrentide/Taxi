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

public class PaymentDAO implements DAO<Long, Payment> {

    @Override
    public Payment save(Payment payment) {
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
        try {
            return DAOUtil.execute(connection -> {
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
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving payment.", e);
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        String sql = getBaseSelectQuery() + " WHERE pay.id = ?";
        try {
            return DAOUtil.execute(connection -> {
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
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding payment by id.", e);
        }
    }

    @Override
    public List<Payment> findAll() {
        String sql = getBaseSelectQuery();
        try {
            return DAOUtil.execute(connection -> {
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
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all payments.", e);
        }
    }

    @Override
    public boolean update(Long id, Payment payment) {
        String sql = "UPDATE payment SET amount = ?, paid_at = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setBigDecimal(1, payment.getAmount());
                    preparedStatement.setTimestamp(2, Timestamp.from(payment.getPaidAt()));
                    preparedStatement.setLong(3, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating payment.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM payment WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting payment.", e);
        }
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

    private Payment mapRowToPayment(ResultSet resultSet) {
        User client;
        try {
            client = new User(
                    resultSet.getString("client_fn"),
                    resultSet.getString("client_ln"),
                    resultSet.getString("client_ph"),
                    new Role(resultSet.getString("client_role_name"))
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
        User driverUser;
        try {
            driverUser = new User(
                    resultSet.getString("driver_fn"),
                    resultSet.getString("driver_ln"),
                    resultSet.getString("driver_ph"),
                    new Role(resultSet.getString("driver_role_name"))
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
        Car car;
        try {
            car = new Car(
                    resultSet.getString("car_brand"),
                    resultSet.getString("car_model"),
                    resultSet.getString("car_plate"),
                    resultSet.getString("car_color"),
                    new CarClass(resultSet.getString("car_class_name"), resultSet.getBigDecimal("car_class_price"))
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
        Driver driver;
        try {
            driver = new Driver(
                    driverUser, car,
                    new DriverStatus(resultSet.getString("driver_status_name")),
                    resultSet.getBigDecimal("driver_rating")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
        PromoCode promo = null;
        String promoCode;
        try {
            promoCode = resultSet.getString("promo_code");
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
        if (promoCode != null) {
            try {
                promo = new PromoCode(promoCode, resultSet.getInt("promo_discount"), resultSet.getBoolean("promo_active"));
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while mapping payment.", e);
            }
        }
        Order order;
        try {
            order = new Order(
                    client, driver,
                    new OrderStatus(resultSet.getString("order_status_name")),
                    promo,
                    new Region(resultSet.getString("region_name"), resultSet.getBigDecimal("region_multiplier")),
                    resultSet.getString("from_address"),
                    resultSet.getString("to_address"),
                    resultSet.getTimestamp("order_date").toInstant()
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
        try {
            return new Payment(
                    order,
                    resultSet.getBigDecimal("payment_amount"),
                    new PaymentType(resultSet.getString("payment_type_name")),
                    resultSet.getTimestamp("payment_date").toInstant()
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment.", e);
        }
    }
}