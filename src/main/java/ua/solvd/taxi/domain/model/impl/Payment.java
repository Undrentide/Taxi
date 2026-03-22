package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class Payment extends Entity {
    private final Order order;
    private final BigDecimal amount;
    private final PaymentType paymentType;
    private final Instant paidAt;
}