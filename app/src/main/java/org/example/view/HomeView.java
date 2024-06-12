package org.example.view;

import org.example.controller.IUserController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * HomeView is the main GUI window that allows users to create, open, and delete
 * spreadsheets.
 * It interacts with the IUserController to handle user actions and update the
 * view accordingly.
 */
public class HomeView extends JFrame implements IHomeView {

    private JButton createSheet;
    private JComboBox<String> openSheetDropdown;
    private JComboBox<String> publishers;
    private JComboBox<String> openSubscriberDropdown;
    private JButton openSheetButton;
    private JButton openSubscribeButton;
    private JButton deleteSheetButton;
    private IUserController controller;

    /**
     * Constructs a HomeView instance, setting up the main GUI window.
     */
    public HomeView() {
        setTitle("Main GUI");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        placeComponents(panel);
        add(panel);
    }

    /**
     * Places and initializes the components within the specified panel.
     *
     * @param panel the JPanel to place the components on.
     */
    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel spreadheetsLabel = new JLabel("Spreadsheets:");
        spreadheetsLabel.setBounds(50, 50, 100, 25);
        panel.add(spreadheetsLabel);

        // Button for creating a new sheet
        createSheet = new JButton("Create Spreadsheet");
        createSheet.setBounds(50, 70, 200, 25);
        panel.add(createSheet);

        // dropdown menu for locally saved sheets
        openSheetDropdown = new JComboBox<>();
        openSheetDropdown.setBounds(50, 110, 200, 25);
        panel.add(openSheetDropdown);

        publishers = new JComboBox<>();
        publishers.setBounds(50, 320, 200, 25);
        panel.add(publishers);

        openSubscriberDropdown = new JComboBox<>();
        openSubscriberDropdown.setBounds(50, 360, 200, 25);
        panel.add(openSubscriberDropdown);

        // Button to open selected sheet
        openSheetButton = new JButton("Open Spreadsheet");
        openSheetButton.setBounds(50, 150, 200, 25);
        panel.add(openSheetButton);

        openSubscribeButton = new JButton("Subscribe and open");
        openSubscribeButton.setBounds(50, 400, 200, 25);
        panel.add(openSubscribeButton);

        // Button to delete selected sheet
        deleteSheetButton = new JButton("Delete Spreadsheet");
        deleteSheetButton.setBounds(50, 190, 200, 25);
        panel.add(deleteSheetButton);

        // Create new sheet with name
        createSheet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sheetName = JOptionPane.showInputDialog(panel, "Enter sheet name:", "Create New Sheet",
                        JOptionPane.PLAIN_MESSAGE);
                if (sheetName != null && !sheetName.trim().isEmpty()) {
                    controller.createNewServerSheet(sheetName);
                } else {
                    JOptionPane.showMessageDialog(panel, "Sheet name cannot be empty", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // open selected sheet
        openSheetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSheet = (String) openSheetDropdown.getSelectedItem();
                if (selectedSheet != null) {
                    controller.openServerSheet(selectedSheet);
                } else {
                    JOptionPane.showMessageDialog(panel, "No sheet selected to open");
                }
            }
        });

        // change publisher
        publishers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (publishers.getSelectedItem() != null) {
                    updateSubscribeSheets((String) publishers.getSelectedItem());
                }
            }
        });
        
        openSubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSheet = (String) openSubscriberDropdown.getSelectedItem();
                if (selectedSheet != null && publishers.getSelectedItem() != null) {
                    controller.openSubscriberSheet(selectedSheet, (String) publishers.getSelectedItem());
                } else {
                    JOptionPane.showMessageDialog(panel, "No sheet selected to open");
                }
            }
        });

        deleteSheetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSheet = (String) openSheetDropdown.getSelectedItem();
                if (selectedSheet != null) {
                    int option = JOptionPane.showOptionDialog(
                            null,
                            "Choose where to delete the sheet from:",
                            "Delete Option",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new Object[] { "Delete Locally", "Delete from Server" },
                            "Delete Locally");

                    if (option == JOptionPane.YES_OPTION) {
                        controller.deleteSheetFromServer(selectedSheet);
                    } else {
                        controller.deleteSheetFromServer(selectedSheet);
                        makeVisible();
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "No sheet selected to delete");
                }
            }
        });

    }

    public void updateSubscribeSheets(String selectedPublisher) {
        List<String> subscribedSheets = controller.accessSheetsFromUser(selectedPublisher);
        openSubscriberDropdown.removeAllItems();
        for (String sheet : subscribedSheets) {
            openSubscriberDropdown.addItem(sheet);
        }
    }

    /**
     * Displays an error message in a dialog box.
     *
     * @param message the error message to display.
     */
    @Override
    public void displayErrorBox(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Updates the list of saved sheets in the dropdown menu.
     */
    @Override
    public void updateSavedSheets() {
        if (controller != null) {
            List<String> savedSheets = controller.getSavedSheetsLocally();
            List<String> serverSheets = controller.getAppUserSheets();
            List<String> listOfPublishers = controller.getPublishersFromServer();
            System.out.println("Updating dropdown with saved sheets: " + savedSheets);
            openSheetDropdown.removeAllItems();
            publishers.removeAllItems();
            for (String sheet : serverSheets) {
                openSheetDropdown.addItem(sheet);
            }

            for (String username : listOfPublishers) {
                publishers.addItem(username);
            }
        }
    }

    /**
     * Adds the controller to this view.
     *
     * @param controller the IUserController instance to add.
     */
    @Override
    public void addController(IUserController controller) {
        this.controller = controller;
    }

    /**
     * Makes the home view visible and updates the list of saved sheets.
     */
    @Override
    public void makeVisible() {
        this.setVisible(true);
        updateSavedSheets();
    }

    /**
     * Disposes of the home page.
     */
    @Override
    public void disposeHomePage() {
        this.dispose();
    }

    // Getter methods
    public JButton getCreateSheetButton() {
        return createSheet;
    }

    public JComboBox<String> getOpenSheetDropdown() {
        return openSheetDropdown;
    }

    public JComboBox<String> getPublishersDropdown() {
        return publishers;
    }

    public JButton getOpenSheetButton() {
        return openSheetButton;
    }

    public JButton getDeleteSheetButton() {
        return deleteSheetButton;
    }

    public IUserController getController() {
        return controller;
    }
}
