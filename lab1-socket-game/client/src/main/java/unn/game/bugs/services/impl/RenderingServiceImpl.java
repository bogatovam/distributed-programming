package unn.game.bugs.services.impl;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.controllers.ErrorsController;
import unn.game.bugs.controllers.GameController;
import unn.game.bugs.models.Point;
import unn.game.bugs.models.message.ResultMessage;
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
    public void buildGameScene(GameDescription gameDescription, Map<String, ClientDescription> allClients,
            ClientDescription clientDescription) {
        try {
            final FXMLLoader loader = new FXMLLoader(this.getClass()
                                                         .getResource("/unn/game/bugs/game.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));

            this.gameController = loader.getController();
            this.context = this.gameController.gameField.getGraphicsContext2D();

            this.scale_x = this.gameController.gameField.widthProperty()
                                                        .divide(gameDescription.getField().length)
                                                        .doubleValue();
            this.scale_y = this.gameController.gameField.heightProperty()
                                                        .divide(gameDescription.getField()[0].length)
                                                        .doubleValue();

            log.debug("Scales [x,y]: [{},{}]", this.scale_x, this.scale_y);

            this.drawGameField(gameDescription, allClients);
            this.drawActivePlayers(new ArrayList<>(allClients.values()));
            this.drawCurrentPlayer(clientDescription);

            stage.show();
        } catch (IOException e) {
            log.error("Something went wrong with loading fxml: " + e.getMessage());
        }
    }

    @Override
    public void buildErrorScene(final String errorMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/unn/game/bugs/error-page.fxml"));

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
                this.drawEmptyCell(i * scale_x + 1, j * scale_y + 1, scale_x - 2, scale_y - 2);

                if (!gameDescription.getField()[i][j].isEmpty()) {
                    if (gameDescription.getField()[i][j].getBug()
                                                        .isAlive()) {
                        ClientDescription clientDescription =
                                allClients.get(gameDescription.getField()[i][j].getBug()
                                                                               .getSetBy());
                        this.drawBugCell(i * scale_x + 5,
                                         j * scale_y + 5,
                                         scale_x - 10,
                                         scale_y - 10,
                                         Color.color(clientDescription.getColor()
                                                                      .getR(),
                                                     clientDescription.getColor()
                                                                      .getG(),
                                                     clientDescription.getColor()
                                                                      .getB()));
                    } else {
                        ClientDescription diedClientDescription =
                                allClients.get(gameDescription.getField()[i][j].getBug()
                                                                               .getSetBy());
                        ClientDescription clientDescription =
                                allClients.get(gameDescription.getField()[i][j].getBug()
                                                                               .getKillBy());
                        this.drawDiedBugCell(i * scale_x + 1,
                                             j * scale_y + 1,
                                             scale_x,
                                             scale_y,
                                             Color.color(diedClientDescription.getColor()
                                                                              .getR(),
                                                         diedClientDescription.getColor()
                                                                              .getG(),
                                                         diedClientDescription.getColor()
                                                                              .getB()),
                                             Color.color(clientDescription.getColor()
                                                                          .getR(),
                                                         clientDescription.getColor()
                                                                          .getG(),
                                                         clientDescription.getColor()
                                                                          .getB()));
                    }

                }
            }
        }
    }

    private void drawDiedBugCell(double x, double y, double w, double h, Color bugColor, Color killerColor) {
        context.setFill(killerColor);
        context.fillRect(x, y, w, h);
        this.drawBugCell(x + 5, y + 5, w - 10, h - 10, bugColor);
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
                                              .collect(Collectors.joining("\n")));
        gameController.players.setVisible(true);
    }

    private void drawCurrentPlayer(ClientDescription current) {
        gameController.currentPlayer.setTextFill(Color.color(current.getColor()
                                                                    .getR(),
                                                             current.getColor()
                                                                    .getG(),
                                                             current.getColor()
                                                                    .getB()));
    }

    @Override
    public void drawActionMessage(ResultMessage message) {
        Platform.runLater(new Thread(() -> {
            switch (message) {
                case ACTION_APPLIED: {
                    gameController.actionMessage.setText("Ход сделан");
                    break;
                }
                case ACTION_PROHIBITED: {
                    gameController.actionMessage.setText("Ход сделать нельзя");
                    break;
                }
                case WIN: {
                    gameController.actionMessage.setText("ВЫ ВЫИГРАЛИ");
                    break;
                }
                case LOSE: {
                    gameController.actionMessage.setText("ВЫ ПРОИГРАЛИ");
                    break;
                }
            }
        }));

    }

    @Override
    public void drawMoveMessage(final String clientId, GameDescription gameDescription) {
        Platform.runLater(new Thread(() -> {
            if (gameDescription.getCurrentPlayerId()
                               .equals(clientId)) {
                gameController.moveMessage.setTextFill(Color.GREEN);
                gameController.moveMessage.setText("Осталось ходов: " + gameDescription.getRemainingMoves());
            } else {
                gameController.moveMessage.setTextFill(Color.RED);
                gameController.moveMessage.setText("Сейчас не ваш ход :(");
            }

            gameController.moveMessage.setVisible(true);
        }));

    }

    public static RenderingServiceImpl getInstance() {
        return instance;
    }
}
