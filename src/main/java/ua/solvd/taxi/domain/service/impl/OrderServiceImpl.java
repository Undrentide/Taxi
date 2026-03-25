package ua.solvd.taxi.domain.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.dal.impl.DriverDAOUtil;
import ua.solvd.taxi.domain.dal.impl.OrderDAOUtil;
import ua.solvd.taxi.domain.dal.impl.OrderStatusDAOUtil;
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
    private final OrderDAOUtil orderDAO;
    private final OrderStatusDAOUtil orderStatusDAO;
    private final DriverDAOUtil driverDAO;

    public OrderServiceImpl(OrderDAOUtil orderDAO, OrderStatusDAOUtil orderStatusDAO, DriverDAOUtil driverDAO) {
        this.orderDAO = orderDAO;
        this.orderStatusDAO = orderStatusDAO;
        this.driverDAO = driverDAO;
    }

    @Override
    public void save(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        if (!"available".equalsIgnoreCase(driver.getDriverStatus().getName())) {
            throw new PersistenceException("The selected driver is currently busy or offline.");
        }
        OrderStatus inProgressStatus = orderStatusDAO.findByName("in_progress")
                .orElseThrow(() -> new PersistenceException("Status 'in_progress' not found."));
        Order order = new Order(client, driver, inProgressStatus, promo, region, from, to, Instant.now());
        orderDAO.save(order);
        String driverPhone = driver.getUser().getPhone();
        if (!driverDAO.updateStatusByPhone(driverPhone, "busy")) {
            throw new PersistenceException("Failed to transition driver to 'busy' status.");
        }
        logger.info("Order created and driver {} is now busy.", driverPhone);
    }
}