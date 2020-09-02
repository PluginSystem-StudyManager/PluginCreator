package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML
    Button btnDummy;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Hello world");
        btnDummy.setOnMouseClicked(e -> {
            System.out.println("Clicked a button");
            btnDummy.setText("You clicked me");
        });
    }
}