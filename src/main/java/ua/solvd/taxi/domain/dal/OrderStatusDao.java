package ua.solvd.taxi.domain.dal;

import ua.solvd.taxi.domain.model.impl.OrderStatus;

import java.util.Optional;

public interface OrderStatusDao extends Dao<OrderStatus> {
    Optional<OrderStatus> findByName(String name);
}