package ua.solvd.taxi.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.model.Entity;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class CarClass extends Entity {
    private final String name;
    private final BigDecimal basePrice;
}