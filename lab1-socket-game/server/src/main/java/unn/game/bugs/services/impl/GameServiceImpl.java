package unn.game.bugs.services.impl;

import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.Game;
import unn.game.bugs.models.Point;
import unn.game.bugs.models.message.ClientMessage;
import unn.game.bugs.models.message.ResultMessage;
import unn.game.bugs.models.message.ServerMessage;
import unn.game.bugs.models.ui.Bug;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.FieldCell;
import unn.game.bugs.models.ui.GameDescription;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.GameService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static unn.game.bugs.models.Constants.FIELD_SIZE_X;
import static unn.game.bugs.models.Constants.FIELD_SIZE_Y;

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

            List<Client> clientListWithDescription = clientList.stream()
                    .peek(client -> {
                        ClientMessage message = client.receiveMessage();
                        client.setClientDescription(message.getClientDescription());
                    })
                    .collect(Collectors.toList());

            Map<String, ClientDescription> clientsDescription = clientListWithDescription.stream()
                    .map(Client::getClientDescription)
                    .collect(Collectors.toMap(ClientDescription::getId, Function.identity()));

            Game newGame = Game.builder()
                    .gameDescription(GameDescription.builder()
                            .gameId(uuid)
                            .field(generateField(clientsDescription))
                            .build())
                    .players(clientListWithDescription)
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
                    log.debug("Receive message {} from client {}", message, client.getClientDescription().getName());
                    // обработка хода должна быть синхронизированной
                    // словарь activeGames хранит актуальное(!) состояние каждой игры
                    // Любое изменение должно фиксироваться в gameDescription соотвествтующего объекта
                    this.makeMove(gameId, message);
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

    public synchronized void makeMove(String gameId, ClientMessage clientMessage) {
        Game game = activeGames.get(gameId);
        // Также делает броадкаст, потому что он тоже должен быть выполнен синхрнизированно
        game.setGameDescription(
                GameDescription.builder()
                .gameId(gameId)
                .field(setBugAtPoint(clientMessage.getPoint(), clientMessage.getClientDescription().getId(), game.getGameDescription().getField()))
                .build()
        );
        Map<String, ClientDescription> clientsDescription = game.getPlayers().stream()
                .map(Client::getClientDescription)
                .collect(Collectors.toMap(ClientDescription::getId, Function.identity()));

        ServerMessage message = ServerMessage.builder()
                .message(ResultMessage.ACTION_APPLIED)
                .allClients(clientsDescription)
                .gameDescription(game.getGameDescription())
                .build();

        for (int i = 0; i < game.getGameDescription().getField().length; i++) {
            for (int j = 0; j < game.getGameDescription().getField()[i].length; j++) {
                if (!game.getGameDescription().getField()[i][j].isEmpty()) {
                    log.debug("{}", game.getGameDescription().getField()[i][j]);
                }
            }
        }

        connectionService.broadcast(game.getPlayers(), message);
    }

    public static GameServiceImpl getInstance() {
        return instance;
    }

    public static FieldCell[][] generateField(Map<String, ClientDescription> clientDescriptionMap) {
        FieldCell[][] field = new FieldCell[FIELD_SIZE_X][FIELD_SIZE_Y];
        System.out.println("AGAIN AGAIN PAIN PAIN HALP");
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                field[i][j] = new FieldCell();
            }
        }

        Random random = new Random();
        int step_x = FIELD_SIZE_X / clientDescriptionMap.size();
        int step_y = FIELD_SIZE_Y / clientDescriptionMap.size();

        int i = 0;
        for (Map.Entry<String, ClientDescription> entry : clientDescriptionMap.entrySet()) {
            int x = (step_x * i) + random.nextInt(step_x);
            int y = (step_y * i) + random.nextInt(step_y);
            field[x][y].setBug(
                    Bug.builder().setBy(entry.getKey()).build()
            );
            i++;
        }
        return field;
    }

    public static FieldCell[][] setBugAtPoint(Point point, final String clientId, FieldCell[][] field) {
        field[point.getX()][point.getY()].setBug(
                Bug.builder().setBy(clientId).build());
        return field;
    }
}
