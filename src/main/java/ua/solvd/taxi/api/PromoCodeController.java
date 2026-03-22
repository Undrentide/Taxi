package ua.solvd.taxi.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.service.PromoCodeService;
import ua.solvd.taxi.domain.service.impl.PromoCodeCodeServiceImpl;

public class PromoCodeController {
    private static final Logger logger = LogManager.getLogger(PromoCodeController.class);
    private final PromoCodeService promoCodeService = new PromoCodeCodeServiceImpl();

    public PromoCode findPromoCodeByCode(String code) {
        PromoCode promoCode = promoCodeService.findPromoCodeByCode(code);
        logger.info("Promo code '{}' is valid. Discount: {}%.", code, promoCode.getDiscountPercent());
        return promoCode;
    }
}