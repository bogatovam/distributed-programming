package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Constants;
import unn.game.bugs.models.Client;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.GameService;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConnectionServiceImpl implements ConnectionService {
    private GameService gameService = new GameServiceImpl();
    private List<Client> pendingClients = new ArrayList<>();

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
    public void addClient(Socket clientSocket) throws IOException {
        // обрабатывать в отдельном потоке одно условие не обязательно: это лишнее усложнение в данном случае
        // если от клиентов необходимо прочитать какое то "приветственное сообщение", то тогда придется
        pendingClients.add(new Client(clientSocket));

        if(pendingClients.size() == Constants.PLAYERS_COUNT ) {
            startGameSession();
        }
    }
}
