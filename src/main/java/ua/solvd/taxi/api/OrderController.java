package ua.solvd.taxi.api;

import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.model.impl.Region;
import ua.solvd.taxi.model.impl.User;
import ua.solvd.taxi.service.OrderService;
import ua.solvd.taxi.service.impl.OrderServiceImpl;

public class OrderController {
    private final OrderService orderService = new OrderServiceImpl();

    public void createOrder(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        orderService.createOrder(client, driver, promo, region, from, to);
    }
}