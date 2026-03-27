package ua.solvd.taxi.domain.model.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.solvd.taxi.domain.model.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Payment extends Entity {
    private final Order order;
    private final BigDecimal amount;
    private final PaymentType paymentType;
    private final Instant paidAt;

    public Payment(UUID id, BigDecimal amount, Order order, Instant paidAt, PaymentType paymentType) {
        super(id);
        this.amount = amount;
        this.order = order;
        this.paidAt = paidAt;
        this.paymentType = paymentType;
    }
}