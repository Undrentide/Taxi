package ua.solvd.taxi.domain.dal.jdbcimpl;

import ua.solvd.taxi.domain.dal.JDBCDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PromoCodeJDBCDAO extends JDBCDAO<PromoCode> {

    @Override
    public PromoCode save(PromoCode promoCode) {
        String sql = "INSERT INTO promo_code (id, code, discount_percent, is_active) VALUES (?, ?, ?, ?)";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, promoCode.getId().toString());
                    preparedStatement.setString(2, promoCode.getCode());
                    preparedStatement.setInt(3, promoCode.getDiscountPercent());
                    preparedStatement.setBoolean(4, promoCode.isActive());
                    preparedStatement.executeUpdate();
                    return promoCode;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving promo code", e);
        }
    }

    @Override
    public Optional<PromoCode> findById(UUID id) {
        String sql = "SELECT promo.id, promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo WHERE promo.id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToPromoCode(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding promo code by id.", e);
        }
    }

    @Override
    public List<PromoCode> findAll() {
        String sql = "SELECT promo.id, promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    List<PromoCode> promoCodeList = new ArrayList<>();
                    while (resultSet.next()) {
                        promoCodeList.add(mapRowToPromoCode(resultSet));
                    }
                    return promoCodeList;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding all promo codes.", e);
        }
    }

    public Optional<PromoCode> findByCode(String code) {
        String sql = "SELECT promo.id, promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo WHERE promo.code = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, code);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        return Optional.of(mapRowToPromoCode(resultSet));
                    }
                    return Optional.empty();
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while finding promo code by code", e);
        }
    }

    @Override
    public boolean update(PromoCode promoCode) {
        String sql = "UPDATE promo_code SET code = ?, discount_percent = ?, is_active = ? WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, promoCode.getCode());
                    preparedStatement.setInt(2, promoCode.getDiscountPercent());
                    preparedStatement.setBoolean(3, promoCode.isActive());
                    preparedStatement.setString(4, promoCode.getId().toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating promo code", e);
        }
    }

    @Override
    public boolean delete(UUID id) {
        String sql = "DELETE FROM promo_code WHERE id = ?";
        try {
            return execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, id.toString());
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting promo code", e);
        }
    }

    private PromoCode mapRowToPromoCode(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        return new PromoCode(
                id,
                resultSet.getString("code"),
                resultSet.getInt("discount_percent"),
                resultSet.getBoolean("is_active")
        );
    }
}