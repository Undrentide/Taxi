package ua.solvd.taxi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.api.CarController;
import ua.solvd.taxi.api.DriverController;
import ua.solvd.taxi.api.OrderController;
import ua.solvd.taxi.api.PromoCodeController;
import ua.solvd.taxi.api.UserController;
import ua.solvd.taxi.domain.dal.Dao;
import ua.solvd.taxi.domain.dal.DriverDao;
import ua.solvd.taxi.domain.dal.OrderStatusDao;
import ua.solvd.taxi.domain.dal.PromoCodeDao;
import ua.solvd.taxi.domain.dal.UserDao;
import ua.solvd.taxi.domain.dal.impl.CarJdbcDao;
import ua.solvd.taxi.domain.dal.impl.DriverJdbcDao;
import ua.solvd.taxi.domain.dal.impl.OrderJdbcDao;
import ua.solvd.taxi.domain.dal.impl.OrderStatusJdbcDao;
import ua.solvd.taxi.domain.dal.impl.PromoCodeJdbcDao;
import ua.solvd.taxi.domain.dal.impl.UserJdbcDao;
import ua.solvd.taxi.domain.dal.impl.UserJacksonDao;
import ua.solvd.taxi.domain.dal.impl.UserJaxbDao;
import ua.solvd.taxi.domain.dal.impl.UserXmlDao;
import ua.solvd.taxi.domain.model.impl.Car;
import ua.solvd.taxi.domain.model.impl.Driver;
import ua.solvd.taxi.domain.model.impl.Order;
import ua.solvd.taxi.domain.model.impl.PromoCode;
import ua.solvd.taxi.domain.model.impl.Region;
import ua.solvd.taxi.domain.model.impl.Role;
import ua.solvd.taxi.domain.model.impl.User;
import ua.solvd.taxi.domain.service.CarService;
import ua.solvd.taxi.domain.service.DriverService;
import ua.solvd.taxi.domain.service.OrderService;
import ua.solvd.taxi.domain.service.PromoCodeService;
import ua.solvd.taxi.domain.service.UserService;
import ua.solvd.taxi.domain.service.impl.CarServiceImpl;
import ua.solvd.taxi.domain.service.impl.DriverServiceImpl;
import ua.solvd.taxi.domain.service.impl.OrderServiceImpl;
import ua.solvd.taxi.domain.service.impl.PromoCodeCodeServiceImpl;
import ua.solvd.taxi.domain.service.impl.UserServiceImpl;

import java.math.BigDecimal;
import java.util.Scanner;

public class EntryPoint {
    private static final Logger logger = LogManager.getLogger(EntryPoint.class);
    private static final Scanner scanner = new Scanner(System.in);

    private static final UserDao userJdbcDao = new UserJdbcDao();
    private static final UserDao userXmlDao = new UserXmlDao();
    private static final UserDao userJaxbDao = new UserJaxbDao();
    private static final UserDao userJacksonDao = new UserJacksonDao();
    private static final Dao<Car> carJdbcDao = new CarJdbcDao();
    private static final DriverDao driverJdbcDao = new DriverJdbcDao();
    private static final Dao<Order> orderJdbcDao = new OrderJdbcDao();
    private static final PromoCodeDao promoCodeJdbcDao = new PromoCodeJdbcDao();
    private static final OrderStatusDao orderStatusJdbcDao = new OrderStatusJdbcDao();

    //We can pass whatever DAO impl we need...
    private static final UserService userService = new UserServiceImpl(userJacksonDao);
    private static final CarService carService = new CarServiceImpl(carJdbcDao);
    private static final DriverService driverService = new DriverServiceImpl(driverJdbcDao);
    private static final OrderService orderService = new OrderServiceImpl(orderJdbcDao, orderStatusJdbcDao, driverJdbcDao);
    private static final PromoCodeService promoService = new PromoCodeCodeServiceImpl(promoCodeJdbcDao);

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