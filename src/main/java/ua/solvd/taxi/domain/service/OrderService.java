package ua.solvd.taxi.domain.service;

import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.User;

public interface OrderService {
    void createOrder(User client, Driver driver, PromoCode promo, Region region, String from, String to);
}