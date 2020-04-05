package unn.game.bugs.services.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.controllers.ErrorsController;
import unn.game.bugs.controllers.GameController;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RenderingServiceImpl implements RenderingService {
    GameController gameController;
    GraphicsContext context;
    private static RenderingServiceImpl instance = new RenderingServiceImpl();

    private RenderingServiceImpl() {
    }

    @Override
    public void buildGameScene(GameDescription gameDescription, List<ClientDescription> allClients, ClientDescription clientDescription) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/unn/game/bugs/game.fxml"
                    )
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            this.gameController = loader.getController();
            this.context = this.gameController.gameField.getGraphicsContext2D();

            this.drawGameField(gameDescription, clientDescription);
            this.drawActivePlayers(allClients);

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

    private void drawGameField(GameDescription gameDescription, ClientDescription clientDescription) {

        double scaleX = this.gameController.gameField.widthProperty().divide(gameDescription.getField().length).doubleValue();
        double scaleY = this.gameController.gameField.heightProperty().divide(gameDescription.getField()[0].length).doubleValue();

        log.debug("Scales [x,y]: [{},{}]", scaleX, scaleY);

        for (int i = 0; i < gameDescription.getField().length; i++) {
            for (int j = 0; j < gameDescription.getField()[i].length; j++) {
                context.setFill(Color.LIGHTGRAY);
                context.fillRect(i * scaleX, j * scaleY, scaleX, scaleY);
                if (!gameDescription.getField()[i][j].isEmpty()) {
                    this.drawBugCell(i * scaleX + 1, j * scaleY + 1, scaleX - 10, scaleY - 10, clientDescription.getColor());
                } else {
                    this.drawEmptyCell(i * scaleX + 1, j * scaleY + 1, scaleX - 2, scaleY - 2);
                }
            }
        }
    }

    private void drawDiedBugCell(double x, double y, double w, double h, Color bugColor, Color killerColor) {
        context.setFill(killerColor);
        context.fillRect(x, y, w, h);
        this.drawBugCell(x, y, w - 10, h - 10, bugColor);
    }

    private void drawBugCell(double x, double y, double w, double h, Color color) {
        context.setFill(color);
        context.fillOval(x, y, w, h);
    }

    private void drawEmptyCell(double x, double y, double w, double h) {
        context.setFill(Color.WHITE);
        context.fillRect(x, y, w, h);
    }

    private void drawActivePlayers(List<ClientDescription> players) {
        gameController.players.setText(players.stream()
                .map(ClientDescription::getName)
                .collect(Collectors.joining("\n"))
        );
        gameController.players.setVisible(true);
    }

    private void drawLoserPlayers(List<ClientDescription> players) {
        gameController.players.setText(players.stream()
                .map(ClientDescription::getName)
                .collect(Collectors.joining("\n"))
        );
        gameController.players.setVisible(true);
    }

    public static RenderingServiceImpl getInstance() {
        return instance;
    }
}
