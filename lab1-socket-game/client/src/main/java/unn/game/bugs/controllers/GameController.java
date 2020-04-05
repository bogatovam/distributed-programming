package unn.game.bugs.controllers;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.ui.GameDescription;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@Data
public class GameController implements Initializable {
    public Label players;
    public Label loserPlayers;
    public Button skipButton;
    public Button stopGameButton;
    public Canvas gameField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
