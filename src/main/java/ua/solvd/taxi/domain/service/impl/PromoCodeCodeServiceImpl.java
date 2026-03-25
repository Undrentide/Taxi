package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.impl.PromoCodeDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.service.PromoCodeService;

public class PromoCodeCodeServiceImpl implements PromoCodeService {
    private final PromoCodeDAO promoCodeDAO = new PromoCodeDAO();

    @Override
    public PromoCode findPromoCodeByCode(String input) {
        return promoCodeDAO.findByCode(input)
                .filter(PromoCode::isActive)
                .orElseThrow(() -> new PersistenceException("Promo code expired."));
    }
}