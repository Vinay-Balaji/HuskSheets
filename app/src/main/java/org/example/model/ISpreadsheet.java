package org.example.model;

import java.util.List;

/**
 * Represents the interface for a spreadsheet.
 */
public interface ISpreadsheet extends IReadOnlySpreadSheet {

    /**
     * Gets a 2D ArrayList of Cell objects representing the cells in the
     * spreadsheet.
     *
     * @return a 2D ArrayList of Cell objects representing the cells in the
     * spreadsheet.
     * @author Theo
     */
    List<List<Cell>> getCells();

    /**
     * Evaluates the given formula and returns the result.
     *
     * @param formula the formula to evaluate.
     * @return the result of evaluating the formula.
     * @author Vinay
     */
    String evaluateFormula(String formula);

    /**
     * Gets the grid.
     * @return a 2D list of Cell
     */
    List<List<Cell>> getGrid();

    /**
     * Sets the value of the cell at the specified row and column.
     *
     * @param row   the row index of the cell.
     * @param col   the column index of the cell.
     * @param value the value to set.
     * @author Vinay
     */
    void setCellValue(int row, int col, String value);

    /**
     * Gets the value of the cell at the specified row and column.
     *
     * @param row the row index of the cell.
     * @param col the column index of the cell.
     * @return the value of the cell.
     * @author Vinay
     */
    String getCellValue(int row, int col);

    /**
     * Sets the raw data of the cell at the specified row and column.
     *
     * @param selRow the row index of the cell.
     * @param selCol the column index of the cell.
     * @param val    the raw data to set.
     * @author Ben
     */
    void setCellRawdata(int selRow, int selCol, String val);

    /**
     * Gets the raw data of the cell at the specified row and column.
     *
     * @param row the row index of the cell.
     * @param col the column index of the cell.
     * @return the raw data of the cell.
     * @author Ben
     */
    String getCellRawdata(int row, int col);

    /**
     * Gets the row in zero index from String input.
     * @param cell a row label
     * @return a row index.
     */
    int getRow(String cell);

    /**
     * Gets the column in zero index from String input.
     * @param cell a column label
     * @return a column index
     */
    int getColumn(String cell);

    /**
     * Adds a published version of the spreadsheet.
     *
     * @param sheet the published spreadsheet to add.
     * @author Tony
     */
    void addPublished(ISpreadsheet sheet);

    /**
     * Adds a subscribed version of the spreadsheet.
     *
     * @param sheet the subscribed spreadsheet to add.
     * @author Tony
     */
    void addSubscribed(ISpreadsheet sheet);

    /**
     * Gets the list of published versions of the spreadsheet.
     *
     * @return a list of published versions of the spreadsheet.
     * @author Tony
     */
    List<ISpreadsheet> getPublishedVersions();

    /**
     * Gets the list of subscribed modified versions of the spreadsheet
     *
     * @return a list of subscribed modified versions of the spreadsheet
     * @author Tony
     */
    List<ISpreadsheet> getSubscribedVersions();

    /**
     * Sets a grid
     *
     * @param updatedGrid 2D List of Cell
     */
    void setGrid(List<List<Cell>> updatedGrid);
}
