package ua.solvd.taxi.domain.service.jdbcimpl;

import ua.solvd.taxi.domain.dal.jdbcimpl.PromoCodeJDBCDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.service.PromoCodeService;

public class PromoCodeCodeServiceJDBCImpl implements PromoCodeService {
    private final PromoCodeJDBCDAO promoCodeJDBCDAO;

    public PromoCodeCodeServiceJDBCImpl(PromoCodeJDBCDAO promoCodeJDBCDAO) {
        this.promoCodeJDBCDAO = promoCodeJDBCDAO;
    }

    @Override
    public PromoCode findPromoCodeByCode(String input) {
        return promoCodeJDBCDAO.findByCode(input)
                .filter(PromoCode::isActive)
                .orElseThrow(() -> new PersistenceException("Promo code expired."));
    }
}