package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.domain.model.impl.PromoCode;

import java.util.Optional;

public interface PromoCodeDao extends Dao<PromoCode> {
    Optional<PromoCode> findByCode(String code);
}