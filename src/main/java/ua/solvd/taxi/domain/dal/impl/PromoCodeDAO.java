package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.AbstractDAO;
import ua.solvd.taxi.domain.dal.DAO;
import ua.solvd.taxi.domain.model.impl.PromoCode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PromoCodeDAO extends AbstractDAO implements DAO<Long, PromoCode> {

    @Override
    public PromoCode save(PromoCode promoCode) throws SQLException {
        String sql = "INSERT INTO promo_code (code, discount_percent, is_active) VALUES (?, ?, ?)";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, promoCode.getCode());
                preparedStatement.setInt(2, promoCode.getDiscountPercent());
                preparedStatement.setBoolean(3, promoCode.isActive());
                preparedStatement.executeUpdate();
                return promoCode;
            }
        });
    }

    @Override
    public Optional<PromoCode> findById(Long id) throws SQLException {
        String sql = "SELECT promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo WHERE promo.id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(mapRowToPromoCode(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public List<PromoCode> findAll() throws SQLException {
        String sql = "SELECT promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo";
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
    }

    public Optional<PromoCode> findByCode(String code) throws SQLException {
        String sql = "SELECT promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo WHERE promo.code = ?";
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
    }

    @Override
    public boolean update(Long id, PromoCode promoCode) throws SQLException {
        String sql = "UPDATE promo_code SET code = ?, discount_percent = ?, is_active = ? WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, promoCode.getCode());
                preparedStatement.setInt(2, promoCode.getDiscountPercent());
                preparedStatement.setBoolean(3, promoCode.isActive());
                preparedStatement.setLong(4, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM promo_code WHERE id = ?";
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate() > 0;
            }
        });
    }

    private PromoCode mapRowToPromoCode(ResultSet resultSet) throws SQLException {
        return new PromoCode(
                resultSet.getString("code"),
                resultSet.getInt("discount_percent"),
                resultSet.getBoolean("is_active")
        );
    }
}