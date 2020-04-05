package unn.game.bugs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import unn.game.bugs.models.Client;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.impl.ConnectionServiceImpl;
import unn.game.bugs.services.impl.GameServiceImpl;

import java.net.URL;
import java.util.ResourceBundle;

public class EntryPointController implements Initializable {

    @FXML
    private TextField clientNameTextField;

    @FXML
    private Button startGameButton;
    @FXML
    private Label errorMessage;

    private final ConnectionService connectionService = new ConnectionServiceImpl();
    private final GameService gameService = new GameServiceImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorMessage.setVisible(false);

        startGameButton.setOnAction(actionEvent -> {
            if (StringUtils.isEmpty(clientNameTextField.getCharacters())) {
                errorMessage.setVisible(true);
            } else {
                Client client = connectionService.createConnection(clientNameTextField.getCharacters().toString());
                gameService.startGame(client);
            }
        });
    }
}
