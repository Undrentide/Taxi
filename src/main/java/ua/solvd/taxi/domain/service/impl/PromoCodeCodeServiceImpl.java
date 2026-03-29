package ua.solvd.taxi.domain.service.impl;

import ua.solvd.taxi.domain.dal.PromoCodeDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.service.PromoCodeService;

public class PromoCodeCodeServiceImpl implements PromoCodeService {
    private final PromoCodeDao promoCodeDao;

    public PromoCodeCodeServiceImpl(PromoCodeDao promoCodeDao) {
        this.promoCodeDao = promoCodeDao;
    }

    @Override
    public PromoCode findPromoCodeByCode(String input) {
        return promoCodeDao.findByCode(input)
                .filter(PromoCode::isActive)
                .orElseThrow(() -> new PersistenceException("Promo code expired."));
    }
}