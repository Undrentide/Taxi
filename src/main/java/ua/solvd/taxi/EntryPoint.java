package ua.solvd.taxi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.solvd.taxi.api.CarController;
import ua.solvd.taxi.api.DriverController;
import ua.solvd.taxi.api.OrderController;
import ua.solvd.taxi.api.PromoCodeController;
import ua.solvd.taxi.api.UserController;
import ua.solvd.taxi.model.impl.Driver;
import ua.solvd.taxi.model.impl.PromoCode;
import ua.solvd.taxi.model.impl.Region;
import ua.solvd.taxi.model.impl.Role;
import ua.solvd.taxi.model.impl.User;

import java.math.BigDecimal;
import java.util.Scanner;

public class EntryPoint {
    private static final Logger logger = LogManager.getLogger(EntryPoint.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserController userController = new UserController();
    private static final CarController carController = new CarController();
    private static final DriverController driverController = new DriverController();
    private static final OrderController orderController = new OrderController();
    private static final PromoCodeController promoCodeController = new PromoCodeController();

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
        Driver driver = driverController.findAvailableDriver();
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