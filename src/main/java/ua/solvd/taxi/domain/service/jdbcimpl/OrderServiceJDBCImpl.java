package ua.solvd.taxi.domain.service.jdbcimpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.domain.dal.jdbcimpl.DriverJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.OrderJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.OrderStatusJDBCDAO;
import ua.solvd.taxi.domain.exception.PersistenceException;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.OrderStatus;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.OrderService;

import java.time.Instant;

public class OrderServiceJDBCImpl implements OrderService {
    private static final Logger logger = LogManager.getLogger(OrderServiceJDBCImpl.class);
    private final OrderJDBCDAO orderJDBCDAO;
    private final OrderStatusJDBCDAO orderStatusJDBCDAO;
    private final DriverJDBCDAO driverJDBCDAO;

    public OrderServiceJDBCImpl(OrderJDBCDAO orderJDBCDAO, OrderStatusJDBCDAO orderStatusJDBCDAO, DriverJDBCDAO driverJDBCDAO) {
        this.orderJDBCDAO = orderJDBCDAO;
        this.orderStatusJDBCDAO = orderStatusJDBCDAO;
        this.driverJDBCDAO = driverJDBCDAO;
    }

    @Override
    public void save(User client, Driver driver, PromoCode promo, Region region, String from, String to) {
        if (!"available".equalsIgnoreCase(driver.getDriverStatus().getName())) {
            throw new PersistenceException("The selected driver is currently busy or offline.");
        }
        OrderStatus inProgressStatus = orderStatusJDBCDAO.findByName("in_progress")
                .orElseThrow(() -> new PersistenceException("Status 'in_progress' not found."));
        Order order = new Order(client, driver, inProgressStatus, promo, region, from, to, Instant.now());
        orderJDBCDAO.save(order);
        String driverPhone = driver.getUser().getPhone();
        if (!driverJDBCDAO.updateStatusByPhone(driverPhone, "busy")) {
            throw new PersistenceException("Failed to transition driver to 'busy' status.");
        }
        logger.info("Order created and driver {} is now busy.", driverPhone);
    }
}