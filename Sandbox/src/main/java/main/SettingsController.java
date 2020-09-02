package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    Button btnDummy;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnDummy.setOnMouseClicked(e -> System.out.println("Button clicked"));
    }
}