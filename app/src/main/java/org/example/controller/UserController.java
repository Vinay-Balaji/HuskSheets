package org.example.controller;

import org.example.model.AppUser;
import org.example.model.Cell;
import org.example.model.Home;
import org.example.model.IAppUser;
import org.example.model.IHome;
import org.example.model.IReadOnlySpreadSheet;
import org.example.model.ISelectedCells;
import org.example.model.ISpreadsheet;
import org.example.model.IReadOnlySpreadSheet;
import org.example.model.Result;
import org.example.model.SelectedCells;
import org.example.model.ServerEndpoint;
import org.example.model.Spreadsheet;
import org.example.view.HomeView;
import org.example.view.IHomeView;
import org.example.view.ILoginView;
import org.example.view.ISheetView;
import org.example.view.SheetView;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;


public class UserController implements IUserController {

    private ILoginView loginPage;
    private ISheetView sheetView;
    private IHomeView homeView;
    private IAppUser appUser;
    private IHome home;

    private ISpreadsheet spreadsheetModel;

    private ISelectedCells selectedCells;

    private String clipboardContent = "";
    private boolean isCutOperation = false;
    private ServerEndpoint serverEndpoint;

    /**
     * Constructor to initialize the UserController.
     * @param loginView the login view.
     */
    public UserController(ILoginView loginView) {
        this.loginPage = loginView;
        loginView.addController(this);
        this.home = new Home();
        this.serverEndpoint = new ServerEndpoint();
    }

    public void registerUser(String username, String password) {
        try {
            if (validateInput(username, password)) {
                IAppUser newUser = new AppUser();
                newUser.setUsername(username);
                newUser.setPassword(password);
                Result registerResult = serverEndpoint.register(newUser);
                if (registerResult.getSuccess()) {
                    openHomeView();
                    this.loginPage.disposeLoginPage();
                    this.appUser = newUser;
                }
                else {
                    this.loginPage.displayErrorBox(registerResult.getMessage());
                }
            }
            else {
                this.loginPage.displayErrorBox("Empty credentials");
            }
        }
        catch (Exception ignored){
        }
    }

    @Override
    public void loginUser(String username, String password) {
        try {
            if (validateInput(username, password)) {
                IAppUser newUser = new AppUser();
                newUser.setUsername(username);
                newUser.setPassword(password);
                Result loginResult = serverEndpoint.login(newUser);
                if (loginResult.getSuccess()) {
                    openHomeView();
                    this.loginPage.disposeLoginPage();
                    this.appUser = newUser;
                }
                else {
                    this.loginPage.displayErrorBox(loginResult.getMessage());
                }
            }
            else {
                this.loginPage.displayErrorBox("Empty credentials");
            }
        }
        catch (Exception ignored){
        }
    }

//    @Override
//    public boolean isUserAuthenticationComplete(String username, String password) {
//        if (validateInput(username, password)) {
//            String message = this.appUser.authenticateUser(username, password);
//            this.loginPage.displayErrorBox(message);
//            if (message.equals("Login successful!")) {
//                this.homeView.makeVisible();
//                this.loginPage.disposeLoginPage();
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean isUserCreatedSuccessfully(String username, String password) {
//        if (validateInput(username, password)) {
//            String message = this.appUser.createAccount(username, password);
//            this.loginPage.displayErrorBox(message);
//            return true;
//        } else {
//            return false;
//        }
//    }

    @Override
    public void setCurrentSheet(ISheetView sheetView) {
        this.sheetView = sheetView;
        this.sheetView.addController(this);
    }

    public ISheetView getCurrentSheet() {
        return this.sheetView;
    }

    @Override
    public void createNewSheet(String name) {
        try {
            Result createSheetResult = serverEndpoint.createSheet(name);
            if (createSheetResult.getSuccess()) {
                this.spreadsheetModel = new Spreadsheet(name);
                this.sheetView = new SheetView(this.spreadsheetModel);
                this.setCurrentSheet(sheetView);
                this.sheetView.makeVisible();
            }
            else {
                this.homeView.displayErrorBox(createSheetResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSheet(IReadOnlySpreadSheet sheet, String path) {
        try {
            this.home.writeXML(sheet, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSheetToServer(IReadOnlySpreadSheet sheet, String name) {
        try {
            String payload = convertSheetToPayload(sheet);
            Result result = serverEndpoint.updatePublished(appUser.getUsername(), name, payload);
            if (result.getSuccess()) {
                System.out.println("Sheet updated successfully on the server.");
            } else {
                System.out.println("Failed to update sheet on the server: " + result.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

//     @Override
//     public void saveSheetToServer(IReadOnlySpreadSheet sheet, String name) {
//         try {
//             String payload = convertSheetToPayload(sheet);
//             serverEndpoint.updatePublished(appUser.getUsername(), name, payload);

// //            HttpClient client = HttpClient.newHttpClient();
// //            String json = String.format("{\"publisher\":\"%s\", \"sheet\":\"%s\", \"payload\":\"%s\"}", "team2", name, payload);
// //
// //            HttpRequest request = HttpRequest.newBuilder()
// //                    .uri(new URI("https://husksheets.fly.dev/api/v1/updatePublished"))
// //                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(("team2:Ltf3r008'fYrV405").getBytes(StandardCharsets.UTF_8)))
// //                    .header("Content-Type", "application/json")
// //                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
// //                    .build();
// //
// //            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
// //            if (response.statusCode() == 200) {
// //                System.out.println("Sheet updated successfully!");
// //            } else {
// //                System.out.println("Failed to update sheet: " + response.body());
// //            }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

    public static String convertSheetToPayload(IReadOnlySpreadSheet sheet) {
        StringBuilder payload = new StringBuilder();
        Cell[][] values = sheet.getCellsObject();
        for (int i = 0; i < sheet.getRows(); i++) {
            for (int j = 0; j < sheet.getCols(); j++) {
                if (values[i][j] != null && !values[i][j].getRawdata().isEmpty()) {
                    String cellValue = values[i][j].isFormula() ? values[i][j].getFormula() : values[i][j].getRawdata();
                    payload.append(String.format("$%s%s %s\\n", getExcelColumnName(j + 1), i + 1, cellValue.replace("\n", "\n").replace("\"", "\\\"")));
                }
            }
        }
        System.out.println("convertSheetToPayload is called here!");
        return payload.toString();
    }
    

    public static String getExcelColumnName(int columnNumber) {
        StringBuilder columnName = new StringBuilder();
        while (columnNumber > 0) {
            int remainder = (columnNumber - 1) % 26;
            columnName.insert(0, (char) (remainder + 'A'));
            columnNumber = (columnNumber - 1) / 26;
        }
        return columnName.toString();
    }

    @Override
    public void handleToolbar(String command) {
        this.sheetView.displayMessage(command + " button clicked");
    }

    @Override
    public void handleStatsDropdown(String selectedStat) {
        // TODO: Implement statistical calculations if needed
    }

    @Override
    public void selectedCells(int[] selectedRows, int[] selectedColumns) {
        if (selectedRows.length > 0 && selectedColumns.length > 0) {
            int startRow = selectedRows[0];
            int endRow = selectedRows[selectedRows.length - 1];
            int startColumn = selectedColumns[0];
            int endColumn = selectedColumns[selectedColumns.length - 1];

            this.selectedCells = new SelectedCells(startRow + 1, endRow + 1, startColumn, endColumn);

            System.out.println("Selected range: (" + (selectedCells.getStartRow()) + ", " +
                    selectedCells.getStartCol() + ") to (" + (selectedCells.getEndRow()) + ", "
                    + selectedCells.getEndCol() + ")");

            if (this.singleCellSelected(this.selectedCells)) {
                this.sheetView.changeFormulaTextField(this.spreadsheetModel.getCellRawdata(
                        this.selectedCells.getStartRow() - 1, this.selectedCells.getStartCol() - 1));
            }
        } else {
            this.selectedCells = new SelectedCells(-1, -1, -1, -1);
        }
    }

    public int getSelectedRowZeroIndex() {
        return selectedCells.getStartRow() - 1;
    }

    public int getSelectedColZeroIndex() {
        return selectedCells.getStartCol() - 1;
    }

    private boolean singleCellSelected(ISelectedCells selectedCells) {
        return selectedCells.getStartRow() == selectedCells.getEndRow() &&
                selectedCells.getStartCol() == selectedCells.getEndCol();
    }

    @Override
    public void openSheet(String path) {
        try {
            this.spreadsheetModel = this.home.readXML(path);
            this.sheetView = new SheetView(spreadsheetModel);
            this.sheetView.makeVisible();
            this.setCurrentSheet(sheetView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSavedSheets() {
        List<String> sheets = new ArrayList<>();
        File folder = new File("HuskSheets/sheets");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".xml")) {
                    sheets.add(file.getName());
                }
            }
        }
        return sheets;
    }

    @Override
    public List<String> getServerSheets() {
        List<String> sheets = new ArrayList<>();
        try {
            String response = serverEndpoint.getSheets();
            sheets = Result.getSheets(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sheets;
    }

    @Override
    public void openServerSheet(String selectedSheet) {
        try {
            this.spreadsheetModel = this.home.readPayload(this.appUser, serverEndpoint, selectedSheet);
            this.sheetView = new SheetView(spreadsheetModel);
            this.setCurrentSheet(sheetView);
            this.sheetView.makeVisible();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSheet(String path) {
        File file = new File("sheets/" + path);
        if (file.exists()) {
            file.delete();
            this.homeView.updateSavedSheets();
        }
    }

    @Override
    public void deleteSheetFromServer(String name) {
        try{
            serverEndpoint.deleteSheet(appUser.getUsername(), name);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String handleReferencingCell(int row, int col, String data) {
        String rawdata = this.spreadsheetModel.getCellRawdata(row, col);
        if (rawdata.startsWith("=")) {
            return this.spreadsheetModel.evaluateFormula(rawdata);
        } else {
            return data;
        }
    }

    @Override
    public IHomeView getHomeView() {
        return this.homeView;
    }

    public void openHomeView() {
        this.homeView = new HomeView();
        homeView.addController(this);
        this.homeView.makeVisible();
    }

    @Override
    public void changeSpreadSheetValueAt(int selRow, int selCol, String val) {
        this.spreadsheetModel.setCellRawdata(selRow, selCol, val);
        if (val.startsWith("=")) {
            this.spreadsheetModel.setCellValue(selRow, selCol, val);
            val = this.spreadsheetModel.evaluateFormula(val);
        }
        this.spreadsheetModel.setCellValue(selRow, selCol, val);
        this.sheetView.updateTable();
    }

    @Override
    public String evaluateFormula(String formula) {
        return this.spreadsheetModel.evaluateFormula(formula);
    }

    @Override
    public void cutCell(int selRow, int selCol) {
        this.clipboardContent = this.spreadsheetModel.getCellRawdata(selRow, selCol);
        this.spreadsheetModel.setCellValue(selRow, selCol, "");
        this.sheetView.updateTable();
        this.isCutOperation = true;
    }

    @Override
    public void copyCell(int selRow, int selCol) {
        this.clipboardContent = this.spreadsheetModel.getCellRawdata(selRow, selCol);
        this.isCutOperation = false;
    }

    @Override
    public void pasteCell(int selRow, int selCol) {
        if (!clipboardContent.isEmpty()) {
            this.spreadsheetModel.setCellValue(selRow, selCol, clipboardContent);
            if (isCutOperation) {
                clipboardContent = "";
                isCutOperation = false;
            }
            this.sheetView.updateTable();
        }
    }

    @Override
    public void getPercentile(int selRow, int selCol) {
        String value = this.spreadsheetModel.getCellValue(selRow, selCol);
        if (value.isEmpty() || value.contains("%")) return;

        try {
            double num = Double.parseDouble(value);
            this.spreadsheetModel.setCellValue(selRow, selCol, "" + (num * 100) + "%");
        } catch (NumberFormatException e) {
            this.spreadsheetModel.setCellValue(selRow, selCol, "Error");
        }
    }

    @Override
    public String getFormula(int row, int col) {
        return this.spreadsheetModel.getCellFormula(row, col);
    }


    private boolean validateInput(String username, String password) {
        return !username.isEmpty() && !password.isEmpty();
    }
}