package unn.game.bugs.services.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.controllers.ErrorsController;
import unn.game.bugs.controllers.GameController;
import unn.game.bugs.models.Point;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RenderingServiceImpl implements RenderingService {
    GameController gameController;
    GraphicsContext context;
    private static RenderingServiceImpl instance = new RenderingServiceImpl();

    private double scale_x;
    private double scale_y;

    private RenderingServiceImpl() {
    }

    @Override
    public void buildGameScene(GameDescription gameDescription, Map<String, ClientDescription> allClients, ClientDescription clientDescription) {
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

            this.scale_x = this.gameController.gameField.widthProperty().divide(gameDescription.getField().length).doubleValue();
            this.scale_y = this.gameController.gameField.heightProperty().divide(gameDescription.getField()[0].length).doubleValue();

            log.debug("Scales [x,y]: [{},{}]", this.scale_x, this.scale_y);

            this.drawGameField(gameDescription, allClients);
            this.drawActivePlayers(new ArrayList<>(allClients.values()));

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

    @Override
    public Point getFieldPointByCanvasCoords(double x, double y) {
        return new Point((int) (x / scale_x), (int) (y / scale_y));
    }

    @Override
    public void drawGameField(GameDescription gameDescription, Map<String, ClientDescription> allClients) {
        for (int i = 0; i < gameDescription.getField().length; i++) {
            for (int j = 0; j < gameDescription.getField()[i].length; j++) {
                context.setFill(Color.LIGHTGRAY);
                context.fillRect(i * scale_x, j * scale_y, scale_x, scale_y);
                if (!gameDescription.getField()[i][j].isEmpty()) {
                    log.debug("{}", gameDescription.getField()[i][j]);
                    ClientDescription clientDescription = allClients.get(gameDescription.getField()[i][j].getBug().getSetBy());
                    this.drawBugCell(i * scale_x + 1, j * scale_y + 1, scale_x - 10, scale_y - 10,
                            Color.color(
                                    clientDescription.getColor().getR(),
                                    clientDescription.getColor().getG(),
                                    clientDescription.getColor().getB())
                    );
                } else {
                    this.drawEmptyCell(i * scale_x + 1, j * scale_y + 1, scale_x - 2, scale_y - 2);
                }
            }
        }
    }

    private void drawDiedBugCell(double x, double y, double w, double h, Color bugColor, Color killerColor) {
        context.setFill(killerColor);
        context.fillRect(x, y, w, h);
        this.drawBugCell(x, y, w - 10, h - 10, bugColor);
    }

    @Override
    public void drawBugCell(double x, double y, double w, double h, Color color) {
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
