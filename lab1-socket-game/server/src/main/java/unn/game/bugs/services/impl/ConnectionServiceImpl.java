package unn.game.bugs.services.impl;

import unn.game.bugs.Constants;
import unn.game.bugs.models.Client;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.GameService;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConnectionServiceImpl implements ConnectionService {
    private GameService gameService = new GameServiceImpl();
    private List<Client> pendingClients = new ArrayList<>();

    private static Logger log = Logger.getLogger(ConnectionServiceImpl.class.getName());
    @Override
    public void addPlayer() {

    }

    @Override
    public void deletePlayer() {

    }

    @Override
    public void startGameSession() {
        gameService.createGame(pendingClients).start();
        pendingClients.clear();
        log.info("Game was started");
    }

    @Override
    public void deleteGameSession() {

    }

    @Override
    public void addClient(Socket clientSocket) {
        // обрабатывать в отдельном потоке одно условие не обязательно: это лишнее усложнение в данном случае
        // если от клиентов необходимо прочитать какое то "приветственное сообщение", то тогда придется
        if(pendingClients.size() < Constants.PLAYERS_COUNT - 1) {
            pendingClients.add(new Client(clientSocket));
            log.info("Unknown client was added in pending list");
        } else {
            startGameSession();
        }
    }
}
