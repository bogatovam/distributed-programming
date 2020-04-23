package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.api.ServerManager;

import java.io.IOException;
import java.net.ServerSocket;

@Slf4j
public class ServerManagerImpl implements ServerManager {
    private ServerSocket serverSocket;
    private final GameService gameService = GameServiceImpl.getInstance();
    private final ConnectionService connectionService = ConnectionServiceImpl.getInstance();

    private static ServerManagerImpl instance = new ServerManagerImpl();

    private ServerManagerImpl() {
    }

    @Override
    public void createAndStartServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        while (true) {
            if (connectionService.addClient(serverSocket.accept())) {
                this.startGameSession();
            }
        }
    }

    @Override
    public void startGameSession() {
        gameService.createGame(connectionService.getPendingClientsAndClear()).start();
        log.debug("Набралось достаточно ожидающих клиентов: начинаем игру");
    }

    @Override
    public void stopServer() throws IOException {
        serverSocket.close();
    }

    public static ServerManagerImpl getInstance() {
        return instance;
    }
}
