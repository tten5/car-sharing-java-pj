package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Engine {

    private final Scanner scanner;

    public Engine() {
        scanner = new Scanner(System.in);
    }

    public void run(String JDBC_DRIVER, String DB_URL) {
        // Database credentials
        // final String USER = "sa";
        // final String PASS = "";



        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 2: Open a connection
            //System.out.println("Connecting to database...");

            //connection = DriverManager.getConnection(DB_URL,USER,PASS);
            Connection connection = DriverManager.getConnection(DB_URL);


            //STEP 3: Execute a query
//            try (Statement statement = connection.createStatement()) {
//                statement.executeUpdate("DROP TABLE CUSTOMER");
//                statement.executeUpdate("DROP TABLE CAR");
//                statement.executeUpdate("DROP TABLE COMPANY");
////                statement.executeUpdate("DELETE FROM CUSTOMER");
////                statement.executeUpdate("DELETE FROM CAR");
////                statement.executeUpdate("DELETE FROM COMPANY");
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

            try (Statement statement = connection.createStatement()) {
                // drop all the table

                // First create table COMPANY if not exist
//                statement.executeUpdate("CREATE TABLE IF NOT EXISTS COMPANY " +
//                        "(id INTEGER, " +
//                        " name VARCHAR(255))");



                statement.executeUpdate("CREATE TABLE IF NOT EXISTS COMPANY " +
                        "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        " name VARCHAR(255) UNIQUE NOT NULL " +
                        ")");

                // alter the constrains
//                statement.executeUpdate("ALTER TABLE COMPANY " +
//                        "ALTER COLUMN name VARCHAR(255) NOT NULL");
//
//                statement.executeUpdate("ALTER TABLE COMPANY " +
//                        "ALTER COLUMN id {INT NOT NULL AUTO_INCREMENT}");
//
//                statement.executeUpdate("ALTER TABLE COMPANY " +
//                        "ADD PRIMARY KEY (id)");
//
//                statement.executeUpdate("ALTER TABLE COMPANY " +
//                        "ADD UNIQUE (name)");
//
                statement.executeUpdate("ALTER TABLE COMPANY " +
                        "ALTER COLUMN id RESTART WITH 1");


                // Then create table CAR if not exist
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS CAR " +
                        "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        " name VARCHAR(255) UNIQUE NOT NULL, " +
                        " company_id INT NOT NULL," +
                        " CONSTRAINT fk_company FOREIGN KEY (company_id)" +
                        " REFERENCES COMPANY(id)" +
                        ")");

                statement.executeUpdate("ALTER TABLE CAR " +
                        "ALTER COLUMN id RESTART WITH 1");

                // Then create table CUSTOMER if not exist
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS CUSTOMER " +
                        "(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                        " name VARCHAR(255) UNIQUE NOT NULL, " +
                        " rented_car_id INT," +
                        " CONSTRAINT fk_car FOREIGN KEY (rented_car_id)" +
                        " REFERENCES CAR(id)" +
                        ")");

                statement.executeUpdate("ALTER TABLE CUSTOMER " +
                        "ALTER COLUMN id RESTART WITH 1");

                statement.executeUpdate("ALTER TABLE CUSTOMER " +
                        "ALTER rented_car_id SET DEFAULT NULL");


            } catch (SQLException e) {
                e.printStackTrace();
            }

            /* *********************************** */
            /* HERE THE MENU */
            /* *********************************** */

            try {
                while (true) {
                    // main menu
                    System.out.println("\n1. Log in as a manager \n2. Log in as a customer \n3. Create a customer \n0. Exit");

                    int mainMenuChoice = Integer.parseInt(scanner.nextLine());

                    if (mainMenuChoice == 0) {
                        break;
                    } else if (mainMenuChoice == 1) {
                        // log in as manager
                        logInAsManager(connection);

                    } else if (mainMenuChoice == 2) {
                        logInAsCustomer(connection);
                    } else if (mainMenuChoice == 3) {
                        createACustomer(connection);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Enable the auto-commit mode so that all changes are automatically saved in the database file
            connection.setAutoCommit(true);

            // STEP 4: Clean-up environment
            // dont need if we use try-catch block
//            statement.close();
//           connection.close();




        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void logInAsManager(Connection connection) throws SQLException {
        // option to do with company

        while (true) {
            System.out.println("\n1. Company list \n2. Create a company \n0. Back");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 0) {
                break;

            } else if (choice == 1) {
                // List all the companies
                listAllCompanies(connection, "manager");

            } else if (choice == 2) {
                // Add new company
                createACompany(connection);
            }
        }

    }

    private void createACompany(Connection connection) {
        System.out.println("Enter the company name:");
        String companyName = scanner.nextLine();

        String insertCompany = "INSERT INTO COMPANY (name) " +
                "VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCompany)) {
            // insert company name

            preparedStatement.setString(1, companyName);

            preparedStatement.executeUpdate();

            System.out.println("The company was created!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int listAllCompanies(Connection connection, String mOrC) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            try (ResultSet companies = statement.executeQuery("SELECT * FROM COMPANY")) {
                // print all the companies
                if (!companies.next()) {
                    System.out.println("The company list is empty");
                    return -1;
                } else {

                    List<String> companyList = new ArrayList<>();
                    System.out.println("\nChoose the company:");

                    while (true) {

                        // retrieve column values
                        int id = companies.getInt("id");
                        String name = companies.getString("name");
                        System.out.println(id + ". " + name);
                        companyList.add(name);

                        if (!companies.next()) {
                            break;
                        }
                    }

                    System.out.println("0. Back");

                    // choose a company
                    int chosenCompany = Integer.parseInt(scanner.nextLine());
                    if (chosenCompany != 0) {
                        String compName = companyList.get(chosenCompany - 1);

                        if (mOrC.equals("manager")) {
                            carMenu(connection, chosenCompany, compName);
                        } else {
                            return listAllCar(connection, chosenCompany, compName, "customer");
                        }
                    }
                }
            }
        }
        return 0;
    }

    private void carMenu(Connection connection, int chosenCompany, String compName) throws SQLException {

        System.out.println("'" + compName + "' company");

        // option to do with car of chosen company

        while (true) {
            System.out.println("\n1. Car list \n2. Create a car \n0. Back");
            int choiceCar = Integer.parseInt(scanner.nextLine());

            if (choiceCar == 0) {
                break;

            } else if (choiceCar == 1) {
                // List all the car
                listAllCar(connection, chosenCompany, compName, "manager");


            } else if (choiceCar == 2) {
                //Add new car
                createACar(connection, chosenCompany);
            }
        }
    }

    private void createACar(Connection connection, int chosenCompany) {
        System.out.println("\nEnter the car name:");
        String carName = scanner.nextLine();

        String insertCompany = "INSERT INTO CAR (name, company_id) " +
                "VALUES (?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCompany)) {
            // insert company name

            preparedStatement.setString(1, carName);
            preparedStatement.setInt(2, chosenCompany);

            preparedStatement.executeUpdate();

            System.out.println("The car was added!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int listAllCar(Connection connection, int chosenCompany, String compName, String mOrC) throws SQLException {

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            // print all the cars
            try (ResultSet cars = statement.executeQuery("SELECT * FROM CAR WHERE company_id = '" + chosenCompany + "'")) {

                // when car list is empty
                if (!cars.next()) {
                    if (mOrC.equals("manager")) {
                        System.out.println("The car list is empty!");
                        return -1;
                    } else {
                        System.out.println("No available cars in the '" + compName + "' company.");
                        return -1;
                    }

                } else {
                    int count = 1;

                    if (mOrC.equals("customer")) {
                        System.out.println("\nChoose a car:");
                    }

                    while (true) {
                        // retrieve column values
                        String name = cars.getString("name");
                        int id = cars.getInt("id");
                        System.out.println(count + ". " + name + ", car id: " + id);
                        count++;

                        if (!cars.next()) {
                            break;
                        }
                    }

                    if (mOrC.equals("customer")) {
                        int chosenCar = Integer.parseInt(scanner.nextLine());
                        cars.absolute(chosenCar);

                        // return car Id
                        return cars.getInt("id");

                    }
                }
            }
        }
        return 0;
    }

    private void createACustomer(Connection connection) {
        // Add new customer

        System.out.println("Enter the customer name:");
        String customerName = scanner.nextLine();

        String insertCustomer = "INSERT INTO CUSTOMER (name) " +
                "VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCustomer)) {
            // insert company name

            preparedStatement.setString(1, customerName);

            preparedStatement.executeUpdate();

            System.out.println("The customer was added!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logInAsCustomer(Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            try (ResultSet customers = statement.executeQuery("SELECT * FROM CUSTOMER")) {
                // print all the customers
                if (!customers.next()) {
                    System.out.println("The customer list is empty!");
                } else {

                    System.out.println("\nCustomer list:");


                    while (true) {
                        // retrieve column values
                        int id = customers.getInt("id");
                        String name = customers.getString("name");
                        System.out.println(id + ". " + name);

                        if (!customers.next()) {
                            break;
                        }
                    }

                    System.out.println("0. Back");

                    // choose a customer
                    int chosenCustomer = Integer.parseInt(scanner.nextLine());


                    if (chosenCustomer != 0) {
                        customers.absolute(chosenCustomer);

                        customerMenu(connection, chosenCustomer);
                    }
                }
            }
        }
    }

    private void customerMenu(Connection connection, int chosenCustomer) {

        // option to do with customer

        try (Statement statement = connection.createStatement()) {

            while (true) {

                // check whether customer has rented a car
                boolean hasRentedCar = false;
                int carId = -1;
                try (ResultSet cus = statement.executeQuery("SELECT * FROM CUSTOMER WHERE id = " + chosenCustomer)) {
                    cus.next();
                    carId = cus.getInt("rented_car_id");
                    if (carId != 0) {
                        hasRentedCar = true;
                    }
                }

                System.out.println("\n1. Rent a car \n2. Return a rented car \n3. My rented car \n0. Back");
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 0) {
                    break;

                } else if (choice == 3) {

                    // check whether customer has rented a car
                    if (!hasRentedCar) {
                        System.out.println("You didn't rent a car!");
                    } else {

                        // if customer has already rented a car

                        try (ResultSet rentedCar = statement.executeQuery("SELECT * FROM CAR WHERE id = " + carId)) {
                            // retrieve column values
                            System.out.println("You rented a car:");

                            rentedCar.next();
                            String name = rentedCar.getString("name");
                            System.out.println(name);

                            int compId = rentedCar.getInt("company_id");

                            try (ResultSet belongToComp = statement.executeQuery("SELECT * FROM COMPANY WHERE id = '" + compId + "'")) {
                                // retrieve column values
                                System.out.println("Company:");

                                belongToComp.next();
                                String compName = belongToComp.getString("name");
                                System.out.println(compName);
                            }
                        }
                    }

                } else if (choice == 1) {
                    // Rent a car
                    // List all companies

                    if (hasRentedCar) {
                        System.out.println("You've already rented a car!");
                    } else {
                        int neededCarId = listAllCompanies(connection, "customer");

                        if (neededCarId != 0) {
                            statement.executeUpdate("UPDATE CUSTOMER SET rented_car_id = " + neededCarId +
                                    " WHERE id = " + chosenCustomer);

                            try (ResultSet car = statement.executeQuery("SELECT * FROM CAR WHERE id = " + neededCarId)) {

                                car.next();
                                String carName = car.getString("name");
                                System.out.println("You rented '" + carName + "'");

                            }
                        }

                    }

                } else if (choice == 2) {
                    // Return a car
                    if (!hasRentedCar) {
                        System.out.println("You didn't rent a car!");
                    } else {
                        statement.executeUpdate("UPDATE CUSTOMER SET rented_car_id = NULL " +
                                "WHERE id = " + chosenCustomer);

                        System.out.println("You've returned a rented car!");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}