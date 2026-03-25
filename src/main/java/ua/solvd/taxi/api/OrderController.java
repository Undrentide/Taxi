package ua.solvd.taxi.api;

import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.OrderService;
import ua.solvd.taxi.domain.service.impl.OrderServiceImpl;

public class OrderController {
    private final OrderService orderService = new OrderServiceImpl();

    public void createOrder(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        orderService.save(client, driver, promo, region, from, to);
    }
}