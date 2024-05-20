package org.example.controller;

import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;

import org.example.model.AppUser;
import org.example.model.IAppUser;
import org.example.model.Spreadsheet;
import org.example.view.HomeView;
import org.example.view.IHomeView;
import org.example.view.ILoginView;
import org.example.view.ISheetView;
import org.example.view.LoginView;
import org.example.view.SheetView;

public class UserController implements IUserController {

    private ILoginView loginPage;

    private ISheetView sheetView;

    private IHomeView homeView;
    private IAppUser appUser;

    public UserController() {
        loginPage = new LoginView();
        loginPage.addController(this);
        sheetView = new SheetView();
        sheetView.addController(this);
        appUser = new AppUser();
        homeView = new HomeView();
        homeView.addController(this);
    }

    @Override
    public boolean isUserAuthenticationComplete(String username, String password) {
        if (validateInput(username, password)) {
            String message = this.appUser.authenticateUser(username, password);
            this.loginPage.displayErrorBox(message);
            if (message.equals("Login successful!")) {
                this.homeView.makeVisible();
                this.loginPage.disposeLoginPage();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isUserCreated(String username, String password) {
        if (validateInput(username, password)) {
            String message = this.appUser.createAccount(username, password);
            this.loginPage.displayErrorBox(message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setCurrentSheet(ISheetView sheetView) {
        this.sheetView = sheetView;
    }

    @Override
    public void createNewSheet() {
        // this.sheetView = new SheetView();
        this.sheetView.makeVisible();
    }

    @Override
    public void saveSheet(Spreadsheet sheet, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(path));
            XMLEncoder encoder = new XMLEncoder(fos);
            encoder.writeObject(sheet);
            encoder.close();
            fos.close();
        } catch (Exception e) {

        }

        return;
    }

    private boolean validateInput(String username, String password) {
        return !username.isEmpty() && !password.isEmpty();
    }
}
