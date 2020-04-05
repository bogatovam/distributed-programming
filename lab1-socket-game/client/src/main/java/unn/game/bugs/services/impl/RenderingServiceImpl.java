package unn.game.bugs.services.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.controllers.ErrorsController;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;

@Slf4j
public class RenderingServiceImpl implements RenderingService {

    private static RenderingServiceImpl instance = new RenderingServiceImpl();

    private RenderingServiceImpl() {
    }

    @Override
    public void buildGameScene() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/unn/game/bugs/game.fxml"));

            Stage stage = new Stage();
            stage.setScene(new Scene(root/*, 450, 450*/));
            stage.show();
        } catch (IOException e) {
            log.error("Something went wrong with loading fxml: " + e.getMessage());
        }
    }

    @Override
    public void buildErrorScene(final String errorMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/unn/game/bugs/error-page.fxml"
                    )
            );

            Stage stage = new Stage();
            stage.setTitle("ERROR");
            stage.setScene(new Scene(loader.load()/*, 450, 450*/));

            ErrorsController controller = loader.getController();
            controller.initData(errorMessage);
            stage.show();
        } catch (IOException e) {
            log.error("Something went wrong with loading fxml: " + e.getMessage());
        }
    }

    public static RenderingServiceImpl getInstance() {
        return instance;
    }
}
