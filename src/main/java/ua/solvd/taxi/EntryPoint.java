package ua.solvd.taxi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.api.CarController;
import ua.solvd.taxi.api.DriverController;
import ua.solvd.taxi.api.OrderController;
import ua.solvd.taxi.api.PromoCodeController;
import ua.solvd.taxi.api.UserController;
import ua.solvd.taxi.domain.dal.UserOtherDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.CarJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.DriverJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.OrderJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.OrderStatusJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.PromoCodeJDBCDAO;
import ua.solvd.taxi.domain.dal.jdbcimpl.UserJDBCDAO;
import ua.solvd.taxi.domain.dal.otherimpl.UserJacksonDAO;
import ua.solvd.taxi.domain.dal.otherimpl.UserJaxbDAO;
import ua.solvd.taxi.domain.dal.otherimpl.UserXMLDAO;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.CarService;
import ua.solvd.taxi.domain.service.DriverService;
import ua.solvd.taxi.domain.service.OrderService;
import ua.solvd.taxi.domain.service.PromoCodeService;
import ua.solvd.taxi.domain.service.UserService;
import ua.solvd.taxi.domain.service.otherimpl.UserServiceOtherImpl;
import ua.solvd.taxi.domain.service.jdbcimpl.CarServiceJDBCImpl;
import ua.solvd.taxi.domain.service.jdbcimpl.DriverServiceJDBCImpl;
import ua.solvd.taxi.domain.service.jdbcimpl.OrderServiceJDBCImpl;
import ua.solvd.taxi.domain.service.jdbcimpl.PromoCodeCodeServiceJDBCImpl;

import java.math.BigDecimal;
import java.util.Scanner;

public class EntryPoint {
    private static final Logger logger = LogManager.getLogger(EntryPoint.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserJDBCDAO userJDBCDAO = new UserJDBCDAO();
    private static final UserOtherDAO userXMLDAO = new UserXMLDAO();
    private static final UserOtherDAO userJaxbDAO = new UserJaxbDAO();
    private static final UserOtherDAO userJacksonDAO = new UserJacksonDAO();
    private static final CarJDBCDAO carJDBCDAO = new CarJDBCDAO();
    private static final DriverJDBCDAO driverJDBCDAO = new DriverJDBCDAO();
    private static final OrderJDBCDAO orderJDBCDAO = new OrderJDBCDAO();
    private static final PromoCodeJDBCDAO promoDAO = new PromoCodeJDBCDAO();
    private static final OrderStatusJDBCDAO orderStatusJDBCDAO = new OrderStatusJDBCDAO();
    /*private static final UserService userService = new UserServiceJDBCImpl(userJDBCDAO);*/
    /*private static final UserService userService = new UserServiceOtherImpl(userXMLDAO);*/
    /*private static final UserService userService = new UserServiceOtherImpl(userJaxbDAO);*/
    private static final UserService userService = new UserServiceOtherImpl(userJacksonDAO);
    private static final CarService carService = new CarServiceJDBCImpl(carJDBCDAO);
    private static final DriverService driverService = new DriverServiceJDBCImpl(driverJDBCDAO);
    private static final OrderService orderService = new OrderServiceJDBCImpl(orderJDBCDAO, orderStatusJDBCDAO, driverJDBCDAO);
    private static final PromoCodeService promoService = new PromoCodeCodeServiceJDBCImpl(promoDAO);
    private static final UserController userController = new UserController(userService);
    private static final CarController carController = new CarController(carService);
    private static final DriverController driverController = new DriverController(driverService);
    private static final OrderController orderController = new OrderController(orderService);
    private static final PromoCodeController promoCodeController = new PromoCodeController(promoService);

    public static void main(String[] args) {
        logger.info("Taxi Management System started.");
        boolean isRunning = true;
        while (isRunning) {
            showMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> handleUserRegistration();
                case "2" -> handleCarList();
                case "3" -> handleDriverList();
                case "4" -> handleOrderPlacement();
                case "0" -> {
                    isRunning = false;
                    logger.info("Exiting application...");
                }
                default -> logger.warn("Invalid selection: {}.", choice);
            }
        }
        scanner.close();
    }

    private static void showMenu() {
        logger.info("\n--- TAXI SERVICE MENU ---");
        logger.info("1. Register New User.");
        logger.info("2. List Fleet Vehicles.");
        logger.info("3. Show Available Drivers.");
        logger.info("4. Place a New Order (Manual Entry).");
        logger.info("0. Exit.");
        logger.info("Select option: ");
    }

    private static void handleUserRegistration() {
        logger.info("Enter details [FirstName, LastName, Phone]:");
        String input = scanner.nextLine();
        String[] data = input.split(",");
        if (data.length == 3) {
            userController.registerNewUser(data[0].trim(), data[1].trim(), data[2].trim(), new Role("CLIENT"));
        } else {
            logger.error("Invalid input format. Use: FirstName, LastName, Phone.");
        }
    }

    private static void handleCarList() {
        carController.findAllCars().forEach(car -> logger.info("Vehicle: {} {} | Plate: {} | Color: {}",
                car.getBrand(),
                car.getModel(),
                car.getLicensePlate(),
                car.getColor()));
    }

    private static void handleDriverList() {
        driverController.findAvailableDrivers()
                .forEach(driver -> logger.info("Driver: {} {} | Plate: {} | Status: {}",
                        driver.getUser().getFirstName(),
                        driver.getUser().getLastName(),
                        driver.getCar().getLicensePlate(),
                        driver.getDriverStatus().getName()));
    }

    private static void handleOrderPlacement() {
        logger.info("Enter client phone number:");
        String phone = scanner.nextLine();
        User client = userController.findUserByPhone(phone);
        if (client == null) {
            logger.error("Client with phone {} not found!", phone);
            return;
        }
        Driver driver = driverController.findAvailableDriver();
        if (driver == null) {
            logger.warn("No available drivers at the moment.");
            return;
        }
        logger.info("Enter promo code (or press Enter to skip):");
        String input = scanner.nextLine();
        PromoCode promoCode = null;
        if (!input.isBlank()) {
            promoCode = promoCodeController.findPromoCodeByCode(input);
        }
        Region region = new Region("Downtown", new BigDecimal("1.2"));
        logger.info("Enter Pickup Address:");
        String from = scanner.nextLine();
        logger.info("Enter Destination Address:");
        String to = scanner.nextLine();
        orderController.createOrder(client, driver, promoCode, region, from, to);
        logger.info("Order created successfully!");
    }
}