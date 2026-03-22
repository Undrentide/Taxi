package ua.solvd.taxi.domain.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.dal.impl.DriverDAO;
import ua.solvd.taxi.domain.dal.impl.OrderDAO;
import ua.solvd.taxi.domain.dal.impl.OrderStatusDAO;
import ua.solvd.taxi.domain.exception.ServiceException;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.OrderStatus;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.OrderService;

import java.sql.SQLException;
import java.time.Instant;

public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderStatusDAO statusDAO = new OrderStatusDAO();
    private final DriverDAO driverDAO = new DriverDAO();

    @Override
    public void createOrder(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        try {
            if (!"available".equalsIgnoreCase(driver.getDriverStatus().getName())) {
                throw new ServiceException("The selected driver is currently busy or offline.");
            }
            OrderStatus inProgressStatus = statusDAO.findByName("in_progress")
                    .orElseThrow(() -> new ServiceException("Status 'in_progress' not found."));
            Order order = new Order(client, driver, inProgressStatus, promo, region, from, to, Instant.now());
            orderDAO.save(order);
            String driverPhone = driver.getUser().getPhone();
            if (!driverDAO.updateStatusByPhone(driverPhone, "busy")) {
                throw new ServiceException("Failed to transition driver to 'busy' status.");
            }
            logger.info("Order created and driver {} is now busy.", driverPhone);
        } catch (SQLException e) {
            throw new ServiceException("Database error during order creation.", e);
        }
    }
}