package ua.solvd.taxi.domain.dal.impl;

import ua.solvd.taxi.domain.dal.JdbcAware;
import ua.solvd.taxi.domain.dal.PromoCodeDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PromoCodeJdbcDao extends JdbcAware implements PromoCodeDao {

    private static final class SqlQuery {
        static final String INSERT = "INSERT INTO promo_code (id, code, discount_percent, is_active) VALUES (?, ?, ?, ?)";

        static final String UPDATE = "UPDATE promo_code SET code = ?, discount_percent = ?, is_active = ? WHERE id = ?";

        static final String DELETE = "DELETE FROM promo_code WHERE id = ?";

        static final String BASE_SELECT = "SELECT promo.id, promo.code, promo.discount_percent, promo.is_active FROM promo_code AS promo";

        static final String FIND_BY_ID = BASE_SELECT + " WHERE promo.id = ?";

        static final String FIND_BY_CODE = BASE_SELECT + " WHERE promo.code = ?";
    }

    @Override
    public PromoCode save(PromoCode promoCode) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.INSERT)) {
                preparedStatement.setString(1, promoCode.getId().toString());
                preparedStatement.setString(2, promoCode.getCode());
                preparedStatement.setInt(3, promoCode.getDiscountPercent());
                preparedStatement.setBoolean(4, promoCode.isActive());
                preparedStatement.executeUpdate();
                return promoCode;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while saving promo code", e);
            }
        });

    }

    @Override
    public Optional<PromoCode> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_ID)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToPromoCode(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding promo code by id.", e);
            }
        });
    }

    @Override
    public List<PromoCode> findAll() {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.BASE_SELECT);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                List<PromoCode> promoCodeList = new ArrayList<>();
                while (resultSet.next()) {
                    promoCodeList.add(mapRowToPromoCode(resultSet));
                }
                return promoCodeList;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding all promo codes.", e);
            }
        });
    }

    @Override
    public Optional<PromoCode> findByCode(String code) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.FIND_BY_CODE)) {
                preparedStatement.setString(1, code);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToPromoCode(resultSet));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while finding promo code by code", e);
            }
        });
    }

    @Override
    public boolean update(PromoCode promoCode) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SqlQuery.UPDATE)) {
                preparedStatement.setString(1, promoCode.getCode());
                preparedStatement.setInt(2, promoCode.getDiscountPercent());
                preparedStatement.setBoolean(3, promoCode.isActive());
                preparedStatement.setString(4, promoCode.getId().toString());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new PersistenceException("Error occurred while updating promo code", e);
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
                throw new PersistenceException("Error occurred while deleting promo code", e);
            }
        });
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