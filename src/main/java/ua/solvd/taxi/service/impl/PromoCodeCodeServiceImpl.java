package ua.solvd.taxi.service.impl;

import ua.solvd.taxi.dal.impl.PromoCodeDAO;
import ua.solvd.taxi.exception.ServiceException;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.service.PromoCodeService;

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