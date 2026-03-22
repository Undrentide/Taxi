package ua.solvd.taxi.service.impl;

import ua.solvd.taxi.dal.impl.OrderDAO;
import ua.solvd.taxi.dal.impl.OrderStatusDAO;
import ua.solvd.taxi.exception.ServiceException;
import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.Order;
import ua.solvd.taxi.model.impl.OrderStatus;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.model.impl.Region;
import ua.solvd.taxi.model.impl.User;
import ua.solvd.taxi.service.OrderService;

import java.sql.SQLException;
import java.time.Instant;

public class OrderServiceImpl implements OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderStatusDAO statusDAO = new OrderStatusDAO();

    @Override
    public void createOrder(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        try {
            if (!"Available".equalsIgnoreCase(driver.getDriverStatus().getName())) {
                throw new ServiceException("The selected driver is currently busy or offline.");
            }
            OrderStatus createdStatus = statusDAO.findByName("in_progress")
                    .orElseThrow(() -> new ServiceException("Status not found."));
            Order order = new Order(client, driver, createdStatus, promo, region, from, to, Instant.now());
            orderDAO.save(order);
        } catch (SQLException e) {
            throw new ServiceException("Error creating order occurred.", e);
        }
    }
}