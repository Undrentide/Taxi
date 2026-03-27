package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.time.Instant;
import java.util.UUID;

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

    public Order(UUID id, User client, Driver driver, OrderStatus orderStatus, PromoCode promoCode, Region region, String fromAddress, String toAddress, Instant createdAt) {
        super(id);
        this.client = client;
        this.createdAt = createdAt;
        this.driver = driver;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.orderStatus = orderStatus;
        this.promoCode = promoCode;
        this.region = region;
    }
}