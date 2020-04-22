package unn.game.bugs.controllers;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.Data;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.impl.GameServiceImpl;

import java.net.URL;
import java.util.ResourceBundle;

@Data
public class GameController implements Initializable {

    public Label players;
    public Label loserPlayers;
    public Button stopGameButton;
    public Canvas gameField;

    private final GameService gameService = GameServiceImpl.getInstance();
    public Label currentPlayer;
    public Label moveMessage;
    public Label actionMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gameField.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            gameService.makeMove(mouseEvent.getX(), mouseEvent.getY());
        });

        stopGameButton.setOnAction(actionEvent -> {
            gameService.stopGame();

            stopGameButton.getScene().getWindow().hide();
        });
    }
}
