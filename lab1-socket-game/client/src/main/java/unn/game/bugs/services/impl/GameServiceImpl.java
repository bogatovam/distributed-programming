package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.message.ClientMessage;
import unn.game.bugs.models.message.ServerMessage;
import unn.game.bugs.models.ui.GameDescription;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;

import static unn.game.bugs.models.Constants.UNPROCESSABLE_MESSAGE_FROM_SERVER;

@Slf4j
public class GameServiceImpl implements GameService {
    private final RenderingService renderingService = RenderingServiceImpl.getInstance();

    private static GameServiceImpl instance = new GameServiceImpl();

    private GameDescription gameDescription;
    private Client client;

    private GameServiceImpl() {
    }

    @Override
    public void startGame(Client client) {
        try {
            this.client = client;

            ServerMessage serverMessage = client.receiveMessage();
            log.info("Receive message: {}", serverMessage);

            this.gameDescription = serverMessage.getGameDescription();
            this.renderingService
                    .buildGameScene(this.gameDescription, serverMessage.getAllClients(), client.getDescription());

            (new Thread(() -> {
                while (true) {
                    try {
                        ServerMessage message = client.receiveMessage();
                        this.renderingService.drawGameField(message.getGameDescription(), serverMessage.getAllClients());
                    } catch (IOException | ClassNotFoundException e) {
                        log.debug(UNPROCESSABLE_MESSAGE_FROM_SERVER + ": " + e.getMessage());
                    }
                }
            })).start();
        } catch (IOException | ClassNotFoundException e) {
            log.debug(UNPROCESSABLE_MESSAGE_FROM_SERVER + ": " + e.getMessage());
        }
    }

    @Override
    public void skipMove() {

    }

    @Override
    public void stopGame() {

    }

    @Override
    public void makeMove(double x, double y) {
        ClientMessage message = ClientMessage.builder()
                .clientDescription(client.getDescription())
                .point(renderingService.getFieldPointByCanvasCoords(x, y))
                .build();
        try {
            client.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameServiceImpl getInstance() {
        return instance;
    }
}
