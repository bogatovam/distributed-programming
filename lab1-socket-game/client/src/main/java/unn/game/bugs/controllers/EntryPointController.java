package unn.game.bugs.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.impl.GameServiceImpl;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class EntryPointController implements Initializable {
    public TextField userName;

    @FXML
    protected void handleStartButtonAction() {
        Thread t = new Thread(
                () -> {
                    GameService client = new GameServiceImpl();
                    try {
                        client.startConnection("127.0.0.1", 8080);

                        Integer myNum = ThreadLocalRandom.current().nextInt(0, 10);
                        client.sendMessage("name" + myNum.toString());
                        System.out.println(client.receiveMessage());
                        client.sendMessage("name" + myNum.toString());
                        System.out.println(client.receiveMessage());
                        System.out.println(client.receiveMessage());
                        System.out.println(client.receiveMessage());
                        System.out.println(client.receiveMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        t.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }
}
