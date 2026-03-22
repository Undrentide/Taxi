package ua.solvd.taxi.service;

import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.model.impl.Region;
import ua.solvd.taxi.model.impl.User;

public interface OrderService {
    void createOrder(User client, Driver driver, PromoCode promo, Region region, String from, String to);
}