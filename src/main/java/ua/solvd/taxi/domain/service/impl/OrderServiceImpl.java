package ua.solvd.taxi.domain.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.dal.DriverDao;
import ua.solvd.taxi.domain.dal.OrderDao;
import ua.solvd.taxi.domain.dal.OrderStatusDao;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.OrderStatus;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.OrderService;

import java.time.Instant;

public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);
    private final OrderDao orderDao;
    private final OrderStatusDao orderStatusDao;
    private final DriverDao driverDao;

    public OrderServiceImpl(OrderDao orderDao, OrderStatusDao orderStatusDao, DriverDao driverDao) {
        this.orderDao = orderDao;
        this.orderStatusDao = orderStatusDao;
        this.driverDao = driverDao;
    }

    @Override
    public void save(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        if (!"available".equalsIgnoreCase(driver.getDriverStatus().getName())) {
            throw new PersistenceException("The selected driver is currently busy or offline.");
        }
        OrderStatus inProgressStatus = orderStatusDao.findByName("in_progress")
                .orElseThrow(() -> new PersistenceException("Status 'in_progress' not found."));
        Order order = new Order(client, driver, inProgressStatus, promo, region, from, to, Instant.now());
        orderDao.save(order);
        String driverPhone = driver.getUser().getPhone();
        if (!driverDao.updateStatusByPhone(driverPhone, "busy")) {
            throw new PersistenceException("Failed to transition driver to 'busy' status.");
        }
        logger.info("Order created and driver {} is now busy.", driverPhone);
    }
}