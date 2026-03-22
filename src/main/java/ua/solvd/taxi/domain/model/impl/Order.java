package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class Order extends Entity {
    private final User client;
    private final Driver driver;
    private final OrderStatus orderStatus;
    private final PromoCode promoCode;
    private final Region region;
    private final String fromAddress;
    private final String toAddress;
    private final Instant createdAt;
}