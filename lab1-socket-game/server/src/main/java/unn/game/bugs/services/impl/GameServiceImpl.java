package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.Game;
import unn.game.bugs.models.message.ClientMessage;
import unn.game.bugs.models.message.ResultMessage;
import unn.game.bugs.models.message.ServerMessage;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.GameService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class GameServiceImpl implements GameService {
    // здесь нет базы, это взамен нее
    private Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private final ConnectionService connectionService = ConnectionServiceImpl.getInstance();

    private static GameServiceImpl instance = new GameServiceImpl();

    private GameServiceImpl() {
    }

    @Override
    public Thread createGame(List<Client> clientList) {
        log.debug("Start creating game with clients: {}", clientList);

        return new Thread(() -> {
            String uuid = UUID.randomUUID().toString();

            Map<String, ClientDescription> clientsDescription = clientList.stream()
                    .map(client -> (ClientMessage) client.receiveMessage())
                    .map(ClientMessage::getClientDescription)
                    .collect(Collectors.toMap(ClientDescription::getId, Function.identity()));

            Game newGame = Game.builder()
                    .gameDescription(new GameDescription(uuid, clientsDescription))
                    .players(clientList)
                    .build();

            // save game in list with all active games
            activeGames.put(uuid, newGame);
            // send init game to all client
            ServerMessage initMessage = ServerMessage.builder()
                    .message(ResultMessage.CONNECTION_SUCCESSFUL)
                    .allClients(clientsDescription)
                    .gameDescription(newGame.getGameDescription())
                    .build();
            // send init game to all client
            connectionService.broadcast(clientList, initMessage);

            newGame.getPlayers()
                    .forEach(player ->
                            getGameProcessThread(newGame.getGameDescription().getGameId(), player).start());
        });
    }

    public Thread getGameProcessThread(String gameId, Client client) {
        return new Thread(() -> {
            while (true) {
                if (!client.getClientSocket().isClosed()) {
                    // ход
                    ClientMessage message = client.receiveMessage();
                    log.debug("Receive message {} from client {}", message, client.getDescription().getName());
                    // обработка хода должна быть синхронизированной
                    // словарь activeGames хранит актуальное(!) состояние каждой игры
                    // Любое изменение должно фиксироваться в gameDescription соотвествтующего объекта
                    this.makeMove(activeGames.get(gameId), message);
                } else {
                    // дисконект, закрываем всех с ошибкой
                    ServerMessage message = ServerMessage.builder().message(ResultMessage.CONNECTION_ABORTED).build();
                    connectionService.broadcast(activeGames.get(gameId).getPlayers(), message);
                    this.finishGame(activeGames.get(gameId));
                }
            }
        });
    }

    public void finishGame(Game game) {
        game.getPlayers().stream()
                .filter(p -> !p.getClientSocket().isClosed())
                .forEach(Client::stopConnection);
    }

    public synchronized void makeMove(Game game, ClientMessage clientMessage) {
        // Также делает броадкаст, потому что он тоже должен быть выполнен синхрнизированно

    }

    public static GameServiceImpl getInstance() {
        return instance;
    }
}
