package org.example.controller;

import org.example.model.*;
import org.example.view.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the methods within the user controller.
 */
public class UserControllerTest {

    @Mock
    private ILoginView loginView;
    @Mock
    private IHomeView homeView;
    @Mock
    private ISheetView sheetView;
    @Mock
    private IAppUser appUser;
    @Mock
    private ISpreadsheet spreadsheetModel;
    @Mock
    private ISelectedCells selectedCells;
    @Mock
    private ServerEndpoint serverEndpoint;

    @InjectMocks
    private UserController userController;

    /**
     * Sets up the mock UserController.
     * @throws Exception irrelevant to actual tests
     */
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(loginView);

        setField(userController, "homeView", homeView);
        setField(userController, "sheetView", sheetView);
        setField(userController, "appUser", appUser);
        setField(userController, "spreadsheetModel", spreadsheetModel);
        setField(userController, "selectedCells", selectedCells);
        setField(userController, "serverEndpoint", serverEndpoint);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Tests the registerUser method for a successful registration.
     * @throws Exception if there is an error during registration
     */
    @Test
    public void testRegisterUser_Success() throws Exception {
        when(serverEndpoint.register(any(IAppUser.class))).thenReturn(new Result(true, null, null));

        userController.registerUser("username", "password");

        verify(loginView).disposeLoginPage();
        assertNotNull(userController.getAppUser());
        verify(loginView, never()).displayErrorBox(anyString());
    }

    /**
     * Tests the registerUser method for a failed registration.
     */
    @Test
    public void testRegisterUser_Failure() {
        try {
            when(serverEndpoint.register(any(IAppUser.class))).thenReturn(new Result(false, "Error", null));
            userController.registerUser("username", "password");
            fail("Exception not thrown");
        } catch (Exception e) {
            assertEquals("Error", e.getMessage());
        }
        // appUser does not change after failed register
        assertEquals(userController.getAppUser(), appUser);
    }

    /**
     * Tests the loginUser method for a successful login.
     * @throws Exception if there is an error during login
     */
    @Test
    public void testLoginUser_Success() throws Exception {
        when(serverEndpoint.login(any(IAppUser.class))).thenReturn(new Result(true, null, null));

        userController.loginUser("username", "password");

        verify(loginView).disposeLoginPage();
        assertNotNull(userController.getAppUser());
        verify(loginView, never()).displayErrorBox(anyString());
    }

    /**
     * Tests the loginUser method for a failed login.
     */
    @Test
    public void testLoginUser_Failure() {
        try {
            when(serverEndpoint.login(any(IAppUser.class))).thenReturn(new Result(false, "Error", null));
            userController.loginUser("username", "password");
            fail("Exception not thrown");
        } catch (Exception e) {
            assertEquals("Error", e.getMessage());
        }

        // Current appuser must not change after failed login
        assertEquals(userController.getAppUser(), appUser);
    }

    /**
     * Tests the getPublishersFromServer method for a successful response.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testGetPublishersFromServer_Success() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("publisher1", null, null, null));
        when(serverEndpoint.getPublishers()).thenReturn(new Result(true, null, arguments));

        List<String> publishers = userController.getPublishersFromServer();

        assertEquals(1, publishers.size());
        assertEquals("publisher1", publishers.get(0));
        verify(homeView, never()).displayErrorBox(anyString());
    }

    /**
     * Tests the getPublishersFromServer method for a failed response.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testGetPublishersFromServer_Failure() throws Exception {
        when(serverEndpoint.getPublishers()).thenReturn(new Result(false, "Error", null));

        List<String> publishers = userController.getPublishersFromServer();

        assertTrue(publishers.isEmpty());
        verify(homeView).displayErrorBox("Error");
    }

    /**
     * Tests the createNewServerSheet method for a successful sheet creation.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testCreateNewServerSheet_Success() throws Exception {
        when(serverEndpoint.createSheet(anyString())).thenReturn(new Result(true, null, null));

        userController.createNewServerSheet("sheetName");

        verify(homeView).disposeHomePage();
        assertNotNull(userController.getSpreadsheetModel());
        verify(homeView, never()).displayErrorBox(anyString());
    }

    /**
     * Tests the createNewServerSheet method for a failed sheet creation.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testCreateNewServerSheet_Failure() throws Exception {
        when(serverEndpoint.createSheet(anyString())).thenReturn(new Result(false, "Error", null));

        userController.createNewServerSheet("sheetName");

        verify(homeView).displayErrorBox("Error");
    }

    /**
     * Tests the saveSheetToServer method for a successful save.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testSaveSheetToServer_Success() throws Exception {
        // Mocking the serverEndpoint to return a successful result
        when(serverEndpoint.updatePublished(anyString(), anyString(), anyString())).thenReturn(
                new Result(true, "Sheet updated successfully", new ArrayList<>()));
        when(appUser.getUsername()).thenReturn("testUser");
        IReadOnlySpreadSheet sheet = mock(IReadOnlySpreadSheet.class);
        userController.saveSheetToServer(sheet, "sheetName");
        verify(sheetView, never()).displayMessage(anyString());
    }

    /**
     * Tests the saveSheetToServer method for a failed save.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testSaveSheetToServer_Failure() throws Exception {
        when(serverEndpoint.updatePublished(anyString(), anyString(), anyString())).thenReturn(
                new Result(false, "Error", null));
        when(appUser.getUsername()).thenReturn("testUser");
        IReadOnlySpreadSheet sheet = mock(IReadOnlySpreadSheet.class);
        userController.saveSheetToServer(sheet, "sheetName");
        verify(sheetView).displayMessage("Error");
    }

    /**
     * Tests the deleteSheetFromServer method for a failed deletion.
     * @throws Exception if there is an error during the request
     */
    @Test
    public void testDeleteSheetFromServer_Failure() throws Exception {
        when(serverEndpoint.deleteSheet(anyString(), anyString())).thenReturn(
                new Result(false, "Error", null));
        when(appUser.getUsername()).thenReturn("testUser");
        userController.deleteSheetFromServer("sheetName");
        verify(homeView).displayErrorBox("Error");
    }

    /**
     * Tests the saveSheetLocally method of UserController.
     * Ensures that a sheet is correctly saved to a local file with the expected content.
     */
    @Test
    public void testSaveSheetLocally() {
        Spreadsheet sheet = new Spreadsheet("Test");
        sheet.setCellValue(0, 0, "0");
        sheet.setCellValue(1, 0, "1");

        String path = "..\\sheets\\writeTestSheet.xml";
        this.userController.saveSheetLocally(sheet, path);

        File file = new File(path);
        assertTrue(file.exists());

        try {
            List<String> lines = Files.readAllLines(Paths.get(path));
            for (String line : lines) {
                line.trim();
            }
            assertTrue(lines.contains("<sheet name=\"writeTestSheet\">"));
            assertTrue(lines.contains("    <cell col=\"0\" row=\"0\">0</cell>"));
            assertTrue(lines.contains("    <cell col=\"0\" row=\"1\">1</cell>"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tests the openSheetLocally method of UserController.
     * Ensures that a sheet is correctly opened from a local file and the content matches the expected values.
     */
    @Test
    public void testOpenSheetLocally() {
        File file = new File("..\\sheets\\readTestSheet.xml");
        this.userController.openSheetLocally(file.getAbsolutePath());

        ISpreadsheet sheet = this.userController.getSpreadsheetModel();
        assertEquals("6", sheet.getCellValue(0, 0));
        assertEquals("7", sheet.getCellValue(1, 0));
        assertEquals("8", sheet.getCellValue(2, 0));
        assertEquals("9", sheet.getCellValue(3, 0));
        assertEquals("10", sheet.getCellValue(4, 0));
        assertEquals("11", sheet.getCellValue(5, 0));
        assertEquals("12", sheet.getCellValue(6, 0));
        assertEquals("13", sheet.getCellValue(7, 0));

        assertEquals("", sheet.getCellValue(0, 1));
    }

    /**
     * Tests the getSavedSheetsLocally method of UserController.
     * Ensures that the list of saved sheets locally is not empty and contains the expected sheet name.
     */
    @Test
    public void testGetSavedSheetsLocally() {
        List<String> sheets = this.userController.getSavedSheetsLocally();
        assertFalse(sheets.isEmpty());
        assertTrue(() -> {
            for (String sheet : sheets) {
                if (sheet.equals("readTestSheet.xml")) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Tests the updateSelectedCells method of UserController.
     * Ensures that the selected cells are updated with the specified value.
     * 
     * @throws Exception if there is an error during the test.
     */
    @Test
    public void testUpdateSelectedCells() throws Exception {
        when(selectedCells.getStartRow()).thenReturn(0);
        when(selectedCells.getEndRow()).thenReturn(1);
        when(selectedCells.getStartCol()).thenReturn(0);
        when(selectedCells.getEndCol()).thenReturn(1);

        userController.updateSelectedCells("value");

        verify(spreadsheetModel, times(4)).setCellRawdata(anyInt(), anyInt(), eq("value"));
    }

    /**
     * Tests the changeSpreadSheetValueAt method.
     */
    @Test
    public void testChangeSpreadSheetValueAt() {
        int row = 1;
        int col = 1;
        String value = "=SUM(A1:B2)";

        userController.changeSpreadSheetValueAt(row, col, value);

        verify(spreadsheetModel).setCellRawdata(row, col, value);
        verify(spreadsheetModel).setCellValue(row, col, value);
        verify(spreadsheetModel).evaluateFormula(value);
        verify(sheetView).updateTable();
    }

    /**
     * Tests the cutCell method.
     */
    @Test
    public void testCutCell() {
        int row = 1;
        int col = 1;
        String value = "cutValue";

        when(spreadsheetModel.getCellRawdata(row, col)).thenReturn(value);

        userController.cutCell(row, col);

        verify(spreadsheetModel).setCellValue(row, col, "");
        verify(sheetView).updateTable();
        assertEquals(value, userController.getClipboardContent());
        assertTrue(userController.isCutOperation());
    }

    /**
     * Tests the copyCell method.
     */
    @Test
    public void testCopyCell() {
        int row = 1;
        int col = 1;
        String value = "copyValue";

        when(spreadsheetModel.getCellRawdata(row, col)).thenReturn(value);

        userController.copyCell(row, col);

        assertEquals(value, userController.getClipboardContent());
        assertFalse(userController.isCutOperation());
    }

    /**
     * Tests the getPercentile method.
     */
    @Test
    public void testGetPercentile() {
        int row = 1;
        int col = 1;
        String value = "0.75";

        when(spreadsheetModel.getCellValue(row, col)).thenReturn(value);

        userController.getPercentile(row, col);

        verify(spreadsheetModel).setCellValue(row, col, "75.0%");
    }

    /**
     * Tests the applyConditionalFormatting method.
     * @throws Exception if there is an error during the application
     */
    @Test
    public void testApplyConditionalFormatting() throws Exception {
        Cell[][] cells = new Cell[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                cells[i][j] = new Cell();
            }
        }
        when(spreadsheetModel.getCellsObject()).thenReturn(cells);
        when(spreadsheetModel.getRows()).thenReturn(100);
        when(spreadsheetModel.getCols()).thenReturn(100);

        userController.applyConditionalFormatting();

        verify(sheetView, times(10000)).highlightCell(anyInt(), anyInt(), any(Color.class));
        verify(sheetView).updateTable();
    }

    /**
     * Tests the setSelectedCells method.
     */
    @Test
    public void testSetSelectedCells() {
        userController.setSelectedCells(new int[] { 1, 3 }, new int[] { 5, 10 });
        assertEquals(userController.getSelectedStartRow(), 1);
        assertEquals(userController.getSelectedEndRow(), 3);
        assertEquals(userController.getSelectedStartCol(), 4);
        assertEquals(userController.getSelectedEndCol(), 9);
    }
}
