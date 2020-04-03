package unn.game.bugs;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartClient extends Application {
    public static void main(String[] args) throws IOException {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/unn/game/bugs/entrypoint.fxml"));
        Scene scene = new Scene(root);
        // scene.getStylesheets().add(getClass().getResource("/unn/game/bugs/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}