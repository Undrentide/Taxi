package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.util.DAOUtil;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PromoCodeDAO implements DAO<Long, PromoCode> {

    @Override
    public PromoCode save(PromoCode promoCode) {
        String sql = "INSERT INTO promo_code (code, discount_percent, is_active) VALUES (?, ?, ?)";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, promoCode.getCode());
                    preparedStatement.setInt(2, promoCode.getDiscountPercent());
                    preparedStatement.setBoolean(3, promoCode.isActive());
                    preparedStatement.executeUpdate();
                    return promoCode;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while saving promo code", e);
        }
    }

    @Override
    public Optional<PromoCode> findById(Long id) {
        String sql = "SELECT promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo WHERE promo.id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
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
        String sql = "SELECT promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo";
        try {
            return DAOUtil.execute(connection -> {
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
        String sql = "SELECT promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo WHERE promo.code = ?";
        try {
            return DAOUtil.execute(connection -> {
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
    public boolean update(Long id, PromoCode promoCode) {
        String sql = "UPDATE promo_code SET code = ?, discount_percent = ?, is_active = ? WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, promoCode.getCode());
                    preparedStatement.setInt(2, promoCode.getDiscountPercent());
                    preparedStatement.setBoolean(3, promoCode.isActive());
                    preparedStatement.setLong(4, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while updating promo code", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM promo_code WHERE id = ?";
        try {
            return DAOUtil.execute(connection -> {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setLong(1, id);
                    return preparedStatement.executeUpdate() > 0;
                }
            });
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while deleting promo code", e);
        }
    }

    private PromoCode mapRowToPromoCode(ResultSet resultSet) {
        try {
            return new PromoCode(
                    resultSet.getString("code"),
                    resultSet.getInt("discount_percent"),
                    resultSet.getBoolean("is_active")
            );
        } catch (SQLException e) {
            throw new PersistenceException("Error occurred while mapping promo code", e);
        }
    }
}