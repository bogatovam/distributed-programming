package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.message.ClientMessage;
import unn.game.bugs.models.message.ResultMessage;
import unn.game.bugs.models.message.ServerMessage;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.api.RenderingService;

@Slf4j
public class GameServiceImpl implements GameService {
    private final RenderingService renderingService = RenderingServiceImpl.getInstance();

    private static GameServiceImpl instance = new GameServiceImpl();

    private Client client;

    private GameServiceImpl() {
    }

    @Override
    public void startGame(Client client) {
        this.client = client;

        ServerMessage serverMessage = client.receiveMessage();

        this.renderingService
                .buildGameScene(serverMessage.getGameDescription(), serverMessage.getAllClients(), client.getDescription());

        this.getGameTread().start();
    }

    @Override
    public void skipMove() {

    }

    @Override
    public void stopGame() {
        this.client.stopConnection();
    }

    @Override
    public void makeMove(double x, double y) {
        ClientMessage message = ClientMessage.builder()
                .clientDescription(client.getDescription())
                .point(renderingService.getFieldPointByCanvasCoords(x, y))
                .build();
        client.sendMessage(message);
    }

    private Thread getGameTread() {
        return new Thread(() -> {
            while (true) {
                ServerMessage message = client.receiveMessage();
                log.info("Receive message: {}", message);

                if (message.getMessage().equals(ResultMessage.ACTION_APPLIED)) {
                    this.renderingService.drawGameField(message.getGameDescription(), message.getAllClients());
                }
            }
        });
    }

    public static GameServiceImpl getInstance() {
        return instance;
    }
}
