package ua.solvd.taxi.service;

import ua.solvd.taxi.model.impl.PromoCode;

public interface PromoCodeService {
    PromoCode findPromoCodeByCode(String code);
}