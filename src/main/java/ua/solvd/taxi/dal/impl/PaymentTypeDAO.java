package ua.solvd.taxi.dal.impl;

import ua.solvd.taxi.dal.AbstractDAO;
import ua.solvd.taxi.dal.DAO;
import ua.solvd.taxi.model.impl.PaymentType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentTypeDAO extends AbstractDAO implements DAO<Long, PaymentType> {

    @Override
    public PaymentType save(PaymentType paymentType) throws SQLException {
        String sql = "INSERT INTO payment_type (name) VALUES (?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, paymentType.getName());
                preparedStatement.executeUpdate();
                return paymentType;
            }
        });
    }

    @Override
    public Optional<PaymentType> findById(Long id) throws SQLException {
        String sql = "SELECT types.name FROM payment_type AS types WHERE types.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToPaymentType(resultSet));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<PaymentType> findAll() throws SQLException {
        String sql = "SELECT types.name FROM payment_type AS types";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<PaymentType> paymentTypeList = new ArrayList<>();
                    while (resultSet.next()) {
                        paymentTypeList.add(mapRowToPaymentType(resultSet));
                    }
                    return paymentTypeList;
                }
            }
        });
    }

    @Override
    public boolean update(Long id, PaymentType paymentType) throws SQLException {
        String sql = "UPDATE payment_type SET name = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, paymentType.getName());
                preparedStatement.setLong(2, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM payment_type WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private PaymentType mapRowToPaymentType(ResultSet resultSet) throws SQLException {
        return new PaymentType(resultSet.getString("name"));
    }
}