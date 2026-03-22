package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

@Getter
@RequiredArgsConstructor
public class PromoCode extends Entity {
    private final String code;
    private final int discountPercent;
    private final boolean isActive;
}