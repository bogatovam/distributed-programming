package unn.game.bugs.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ErrorsController {
    @FXML
    private Label errorMessage;

    public void initData(String errorMessage) {
        this.errorMessage.setText(errorMessage);
    }
}
