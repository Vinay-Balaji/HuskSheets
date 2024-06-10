package org.example.server;

import org.example.controller.UserController;
import org.example.model.*;

import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * REST API server for managing publishers, sheets, and related operations.
 */
@RestController
@RequestMapping("/api/v1")
public class server {

//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private SheetRepository sheetRepository;


    /**
     * List of all available users
     */
    private List<IAppUser> availUsers = new ArrayList<>();

    // Method to decode the Basic Auth header
    /**
     * 
     * @param authHeader
     * @author Tony
     */
    private String[] decodeBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }

        byte[] decodedBytes = Base64.getDecoder().decode(authHeader.substring(6));
        String decodedString = new String(decodedBytes);
        return decodedString.split(":", 2);
    }

    /**
     * @author Tony
     * @param credentials
     */
    private void validateCredentials(String[] credentials) {
        if (credentials == null || credentials.length != 2) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    /**
     * Checks if a sheet exists for a publisher.
     *
     * @param sheet     the sheet name.
     * @param publisher the publisher name.
     * @return true if the sheet exists, false otherwise.
     * @author Tony
     */
    private boolean hasSheet(String sheet, String publisher) {
        for (IAppUser user : availUsers) {
            if (user.getUsername().equals(publisher) && user.doesSheetExist(sheet)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a user by username.
     *
     * @param username the username.
     * @return the user if found, null otherwise.
     * @author Ben
     */
    private IAppUser findUser(String username) {
        for (IAppUser user : this.availUsers) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * @author Ben
     * @param username
     * @param password
     * @return
     */
    private boolean existingUser(String username, String password) {
        for (IAppUser user : availUsers) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @author Tony
     * @param username
     * @return
     */
    private boolean findByUsername(String username) {
        for (IAppUser user : availUsers) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @author Ben
     * @param authHeader
     * @return
     */
    @GetMapping("/getPublishers")
    public ResponseEntity<?> getPublishers(@RequestHeader("Authorization") String authHeader) {
        try {
            // Decode the Basic Auth header
            String[] credentials = decodeBasicAuth(authHeader);
            validateCredentials(credentials);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(401).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
        List<Argument> listOfArgument = new ArrayList<>();

        for (IAppUser user : availUsers) {
            listOfArgument.add(new Argument(user.getUsername(), null, null, null));
        }

        return ResponseEntity.ok(new Result(
                true, null, listOfArgument));
    }

    /**
     * Creates a new sheet for a specified publisher.
     *
     * @param authHeader the authorization header containing the credentials.
     * @param argument   the argument containing the sheet details.
     * @return a ResponseEntity containing the result of the sheet creation.
     * @author Ben
     */
    @PostMapping("/createSheet")
    public ResponseEntity<Result> createSheet(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody Argument argument) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
            validateCredentials(credentials);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            return ResponseEntity.status(401).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
        String username = credentials[0];
        String publisher = argument.getPublisher();
        String sheet = argument.getSheet();
        if (!publisher.equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                    false, "Unauthorized: sender is not owner of sheet", new ArrayList<>()));
        } else if (sheet.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result(
                    false, "Sheet name cannot be blank", new ArrayList<>()));
        } else if (hasSheet(sheet, publisher)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Result(
                    false, "Sheet already exists: " + sheet, new ArrayList<>()));
        } else {
            IAppUser user = findUser(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                        false, "User not found", new ArrayList<>()));
            }
            user.addSheet(sheet);
            return ResponseEntity.status(HttpStatus.CREATED).body(new Result(
                    true, "Sheet created successfully", new ArrayList<>()));
        }
    }

    /**
     * @author Ben
     * @param authHeader
     * @param argument
     * @return
     */
    @PostMapping("/deleteSheet")
    public ResponseEntity<Result> deleteSheet(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody Argument argument) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
            validateCredentials(credentials);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
        String username = credentials[0];
        String publisher = argument.getPublisher();
        String sheet = argument.getSheet();
        IAppUser user = findUser(username);
        if (!publisher.equals(username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                    false, "Unauthorized: sender is not owner of sheet", new ArrayList<>()));
        } else if (sheet.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result(
                    false, "Sheet name cannot be blank", new ArrayList<>()));
        }
        else if (!user.doesSheetExist(sheet)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result(
                    false, "Sheet does not exist: " + sheet, new ArrayList<>()));
        }
        else {
            user.removeSheet(sheet);
            return ResponseEntity.status(HttpStatus.CREATED).body(new Result(
                    true, "Sheet deleted successfully", new ArrayList<>()));
        }
    }

    /**
     * Retrieves all sheets for a specified publisher.
     *
     * @param authHeader the authorization header containing the credentials.
     * @param argument   the argument containing the publisher details.
     * @return a ResponseEntity containing the result of the sheets retrieval.
     * @author Tony
     */
    @PostMapping("/getSheets")
    public ResponseEntity<Result> getSheets(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Argument argument) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
            validateCredentials(credentials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                false, e.getMessage(), new ArrayList<>()));
        }
            List<Argument> sheets = new ArrayList<>();;
            String publisher = argument.getPublisher();
            IAppUser user = findUser(publisher);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                    false, "User not found", new ArrayList<>()));
        }
        for(ISpreadsheet sheet : user.getSheets()){
            sheets.add(new Argument(publisher, sheet.getName(), null, null));
        }
        return ResponseEntity.ok(new Result(true, "Sheets retrieved successfully", sheets));
    }

    /**
     * Updates a published sheet for a specified publisher.
     *
     * @param authHeader the authorization header containing the credentials.
     * @param argument   the argument containing the sheet details.
     * @return a ResponseEntity containing the result of the sheet update.
     * @author - Tony
     */
    @PostMapping("/updatePublished")
    public ResponseEntity<Result> updatePublished(@RequestHeader("Authorization") String authHeader,
                                                  @RequestBody Argument argument) {

        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
            validateCredentials(credentials);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
            String publisher = argument.getPublisher();
            String sheet = argument.getSheet();
            String payload = argument.getPayload();
            System.out.println("Publisher: " + publisher);
            System.out.println("Sheet: " + sheet);
            System.out.println("Payload: " + payload);
            IAppUser user = findUser(publisher);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                        false, "User not found", new ArrayList<>()));
            }

            for (ISpreadsheet existingSheet : user.getSheets()) {
                if (existingSheet.getName().equals(sheet)) {
                    List<List<String>> data = Home.convertStringTo2DArray(payload);
                    List<List<Cell>> updatedGrid = new ArrayList<>();
                    for (int i = 0; i < existingSheet.getRows(); i++) {
                        ArrayList<Cell> row = new ArrayList<>();
                        for (int j = 0; j < existingSheet.getCols(); j++) {
                            row.add(new Cell(""));
                        }
                        updatedGrid.add(row);
                    }

                    for (List<String> ls : data) {
                        updatedGrid.get(Integer.parseInt(ls.get(0))).get(Integer.parseInt(ls.get(1))).setValue(ls.get(2));
                        updatedGrid.get(Integer.parseInt(ls.get(0))).get(Integer.parseInt(ls.get(1))).setRawData(ls.get(2));
                    }
                    existingSheet.setGrid(updatedGrid);

                    ISpreadsheet updatedVersion = new Spreadsheet(existingSheet.getName());
                    List<List<Cell>> grid = existingSheet.getCells();
                    for (int i = 0; i < grid.size(); i++) {
                        for(int j = 0; j < grid.get(i).size(); j++) {
                            updatedVersion.setCellValue(i, j, grid.get(i).get(j).getValue());
                            updatedVersion.setCellRawdata(i, j, grid.get(i).get(j).getRawdata());
                        }
                    }
                    existingSheet.addPublished(updatedVersion);
                    return ResponseEntity.ok(new Result(true, "Sheet updated successfully", new ArrayList<>()));
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                    false, "Sheet not found", new ArrayList<>()));
        }

        /**
         * @author Ben
         * @param authHeader
         * @param argument
         * @return
         */
    @PostMapping("/updateSubscription")
    public ResponseEntity<Result> updateSubscription(@RequestHeader("Authorization") String authHeader,
                                                  @RequestBody Argument argument) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);

        try {
            validateCredentials(credentials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
        String publisher = argument.getPublisher();
        String sheet = argument.getSheet();
        String payload = argument.getPayload();
        IAppUser user = findUser(publisher);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                    false, "User not found", new ArrayList<>()));
        }

        for (ISpreadsheet existingSheet : user.getSheets()) {
            if (existingSheet.getName().equals(sheet)) {
                List<List<String>> data = Home.convertStringTo2DArray(payload);
                List<List<Cell>> updatedGrid = new ArrayList<>();
                for (int i = 0; i < existingSheet.getRows(); i++) {
                    ArrayList<Cell> row = new ArrayList<>();
                    for (int j = 0; j < existingSheet.getCols(); j++) {
                        row.add(new Cell(""));
                    }
                    updatedGrid.add(row);
                }
                for (List<String> ls : data) {
                    updatedGrid.get(Integer.parseInt(ls.get(0))).get(Integer.parseInt(ls.get(1))).setValue(ls.get(2));
                    updatedGrid.get(Integer.parseInt(ls.get(0))).get(Integer.parseInt(ls.get(1))).setRawData(ls.get(2));
                }
                existingSheet.setGrid(updatedGrid);

                ISpreadsheet updatedVersion = new Spreadsheet(existingSheet.getName());
                List<List<Cell>> grid = existingSheet.getCells();
                for (int i = 0; i < grid.size(); i++) {
                    for (int j = 0; j < grid.get(i).size(); j++) {
                        updatedVersion.setCellValue(i, j, grid.get(i).get(j).getValue());
                        updatedVersion.setCellRawdata(i, j, grid.get(i).get(j).getRawdata());
                    }
                }
                existingSheet.addSubscribed(updatedVersion);
                return ResponseEntity.ok(new Result(true, "Sheet updated successfully", new ArrayList<>()));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                false, "Sheet not found", new ArrayList<>()));
    }

    /**
     * Registers a new publisher.
     *
     * @param authHeader the authorization header containing the credentials.
     * @return a ResponseEntity containing the result of the registration.
     * @author Tony
     */
    @GetMapping("/register")
    public ResponseEntity<Result> register(@RequestHeader("Authorization") String authHeader) {

        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
            validateCredentials(credentials);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                        false, e.getMessage(), new ArrayList<>()));
            }
        String username = credentials[0];
        String password = credentials[1];
        System.out.println(username + ": " + password);

        // Check if the user already exists
        if (findByUsername(username)) {
            return ResponseEntity.status(401).body(new Result(
                    false, "User already exists", new ArrayList<>()));
        }
        // Create a new user
        AppUser newUser = new AppUser(username, password);
        availUsers.add(newUser);
        return ResponseEntity.ok(new Result(
                true, "Publisher registered successfully", new ArrayList<>()));
    }

    /**
     * Logs in a user.
     *
     * @param authHeader the authorization header containing the credentials.
     * @return a ResponseEntity containing the result of the login.
     * @author Ben
     */
    @GetMapping("/login")
    public ResponseEntity<Result> login(@RequestHeader("Authorization") String authHeader) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
           validateCredentials(credentials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
        String username = credentials[0];
        String password = credentials[1];
        System.out.println(username + ": " + password);

        // Check if the user already exists
        if (existingUser(username, password)) {
            return ResponseEntity.ok(new Result(
                    true, "Publisher logged in successfully", new ArrayList<>()));
        }
        else {
            return ResponseEntity.status(401).body(new Result(
                    false, "Wrong username or password", new ArrayList<>()));
        }
    }

    /**
     * Retrieves updates for a subscription.
     *
     * @param authHeader the authorization header containing the credentials.
     * @param argument   the argument containing the subscription details.
     * @return a ResponseEntity containing the result of the updates retrieval.
     * @author Tony
     */
    @PostMapping("/getUpdatesForSubscription")
    public ResponseEntity<?> getUpdatesForSubscription(@RequestHeader("Authorization") String authHeader,
                                                       @RequestBody Argument argument) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
            validateCredentials(credentials);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                        false, e.getMessage(), new ArrayList<>()));
            }

        String publisher = argument.getPublisher();
        String sheet = argument.getSheet();
        String id = argument.getId();
        System.out.println("User: " + publisher + ", Sheet Name: " + sheet + ", ID: " + id);
        IAppUser user = findUser(publisher);
        if (user == null) {
            System.out.println("User not found: " + publisher);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                    false, "User not found", new ArrayList<>()));
        }
        List<Argument> arguments = new ArrayList<>();
        for (ISpreadsheet existingSheet : user.getSheets()) {
            if (existingSheet.getName().equals(sheet)) {
                List<ISpreadsheet> versions = existingSheet.getPublishedVersions();
                System.out.println("Found sheet: " + sheet + ", Versions: " + versions.size());
                for (int i = Integer.parseInt(id); i < versions.size(); i++) {
                    String payload = Spreadsheet.convertSheetToPayload(versions.get(i));
                    System.out.println("Payload for version " + i + ": " + payload);
                    Argument arg = new Argument(publisher, sheet, String.valueOf(i), payload);
                    arguments.add(arg);
                }
                System.out.println("Returning updates: " + arguments.size());
                return ResponseEntity.ok(new Result(true, "Updates received", arguments));
            }
        }
        System.out.println("Sheet not found: " + sheet);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                false, "Sheet not found", new ArrayList<>()));
    }

    /**
     * Retrieves updates for a subscription.
     *
     * @param authHeader the authorization header containing the credentials.
     * @param argument   the argument containing the subscription details.
     * @return a ResponseEntity containing the result of the updates retrieval.
     * @author Tony
     */
    @PostMapping("/getUpdatesForPublished")
    public ResponseEntity<?> getUpdatesForPublished(@RequestHeader("Authorization") String authHeader,
                                                    @RequestBody Argument argument) {
        // Decode the Basic Auth header
        String[] credentials = decodeBasicAuth(authHeader);
        try {
          validateCredentials(credentials);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Result(
                    false, e.getMessage(), new ArrayList<>()));
        }
        String publisher = argument.getPublisher();
        String sheet = argument.getSheet();
        String id = argument.getId();
        System.out.println("User: " + publisher + ", Sheet Name: " + sheet + ", ID: " + id);
        IAppUser user = findUser(publisher);

        if (user == null) {
            System.out.println("User not found: " + publisher);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                    false, "User not found", new ArrayList<>()));
        }

        List<Argument> arguments = new ArrayList<>();
        for (ISpreadsheet existingSheet : user.getSheets()) {
            if (existingSheet.getName().equals(sheet)) {
                List<ISpreadsheet> versions = existingSheet.getSubscribedVersions();
                System.out.println("Found sheet: " + sheet + ", Versions: " + versions.size());
                for (int i = Integer.parseInt(id); i < versions.size(); i++) {
                    String payload = Spreadsheet.convertSheetToPayload(versions.get(i));
                    System.out.println("Payload for version " + i + ": " + payload);
                    Argument arg = new Argument(publisher, sheet, String.valueOf(i), payload);
                    arguments.add(arg);
                }
                System.out.println("Returning updates: " + arguments.size());
                return ResponseEntity.ok(new Result(true, "Updates received", arguments));
            }
        }

        System.out.println("Sheet not found: " + sheet);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Result(
                false, "Sheet not found", new ArrayList<>()));
    }
}
