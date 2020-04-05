package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.message.ServerMessage;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;

@Slf4j
public class GameServiceImpl implements GameService {
    private final RenderingService renderingService = RenderingServiceImpl.getInstance();

    private static GameServiceImpl instance = new GameServiceImpl();

    private GameServiceImpl() {
    }

    @Override
    public void startGame(Client client) {
        try {
            log.info("Receive message: {}", (ServerMessage) client.receiveMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static GameServiceImpl getInstance() {
        return instance;
    }
}
