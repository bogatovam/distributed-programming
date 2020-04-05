package unn.game.bugs.controllers;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.impl.GameServiceImpl;

import java.net.URL;
import java.util.ResourceBundle;

@Data
public class GameController implements Initializable {
    public Label players;
    public Label loserPlayers;
    public Button skipButton;
    public Button stopGameButton;
    public Canvas gameField;

    private final GameService gameService = GameServiceImpl.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gameField.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            gameService.makeMove(mouseEvent.getX(), mouseEvent.getY());
        });

        skipButton.setOnAction(actionEvent -> {
            gameService.skipMove();
        });

        stopGameButton.setOnAction(actionEvent -> {
            gameService.stopGame();
        });
    }
}
