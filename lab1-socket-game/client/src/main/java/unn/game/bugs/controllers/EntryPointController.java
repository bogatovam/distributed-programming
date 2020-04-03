package unn.game.bugs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.RenderingService;
import unn.game.bugs.services.impl.ConnectionServiceImpl;
import unn.game.bugs.services.impl.RenderingServiceImpl;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EntryPointController implements Initializable {

    @FXML
    private TextField userNameTextField;

    @FXML
    private Button startGameButton;
    @FXML
    private Label errorMessage;

    private final ConnectionService connectionService = new ConnectionServiceImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorMessage.setVisible(false);

        startGameButton.setOnAction(actionEvent -> {
            if (StringUtils.isEmpty(userNameTextField.getCharacters())) {
                errorMessage.setVisible(true);
            } else {
                connectionService.createConnection();
            }
        });
    }
}
