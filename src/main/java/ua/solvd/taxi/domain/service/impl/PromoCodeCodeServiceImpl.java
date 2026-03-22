package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.PromoCodeDAO;
import ua.solvd.taxi.domain.exception.ServiceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.service.PromoCodeService;

import java.sql.SQLException;

public class PromoCodeCodeServiceImpl implements PromoCodeService {
    private final PromoCodeDAO promoCodeDAO = new PromoCodeDAO();

    @Override
    public PromoCode findPromoCodeByCode(String input) {
        try {
            return promoCodeDAO.findByCode(input)
                    .filter(PromoCode::isActive)
                    .orElseThrow(() -> new ServiceException("Promo code expired."));
        } catch (SQLException e) {
            throw new ServiceException("Error validating promo code.", e);
        }
    }
}