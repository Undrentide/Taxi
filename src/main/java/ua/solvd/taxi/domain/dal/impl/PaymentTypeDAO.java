package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PaymentType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentTypeDAO extends AbstractDAO implements DAO<Long, PaymentType> {

    @Override
    public PaymentType save(PaymentType paymentType) {
        String sql = "INSERT INTO payment_type (name) VALUES (?)";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, paymentType.getName());
                    preparedStatement.executeUpdate();
                    return paymentType;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving payment type.", e);
        }
    }

    @Override
    public Optional<PaymentType> findById(Long id) {
        String sql = "SELECT types.name FROM payment_type AS types WHERE types.id = ?";
        try {
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
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding payment type by id.", e);
        }
    }

    @Override
    public List<PaymentType> findAll() {
        String sql = "SELECT types.name FROM payment_type AS types";
        try {
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
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all payment types.", e);
        }
    }

    @Override
    public boolean update(Long id, PaymentType paymentType) {
        String sql = "UPDATE payment_type SET name = ? WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, paymentType.getName());
                    preparedStatement.setLong(2, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating payment type.", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM payment_type WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting payment type.", e);
        }
    }

    private PaymentType mapRowToPaymentType(ResultSet resultSet) {
        try {
            return new PaymentType(resultSet.getString("name"));
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping payment type.", e);
        }
    }
}