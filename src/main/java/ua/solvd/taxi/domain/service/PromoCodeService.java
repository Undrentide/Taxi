package ua.solvd.taxi.domain.service;

import ua.solvd.taxi.domain.model.impl.PromoCode;

public interface PromoCodeService {
    PromoCode findPromoCodeByCode(String code);
}