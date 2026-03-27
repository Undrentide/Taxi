package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PromoCode extends Entity {
    private final String code;
    private final int discountPercent;
    private final boolean isActive;

    public PromoCode(UUID id, String code, int discountPercent, boolean isActive) {
        super(id);
        this.code = code;
        this.discountPercent = discountPercent;
        this.isActive = isActive;
    }
}