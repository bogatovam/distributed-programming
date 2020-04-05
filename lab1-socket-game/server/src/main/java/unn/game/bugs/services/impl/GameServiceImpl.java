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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static unn.game.bugs.models.Constants.UNPROCESSABLE_MESSAGE_FROM_CLIENT;

@Slf4j
public class GameServiceImpl implements GameService {
    // здесь нет базы, это взамен нее
    private ConcurrentHashMap<String, Game> activeGames = new ConcurrentHashMap<>();
    private final ConnectionService connectionService = ConnectionServiceImpl.getInstance();

    private static GameServiceImpl instance = new GameServiceImpl();

    private GameServiceImpl() {
    }

    @Override
    public Thread createGame(List<Client> clientList) {
        log.debug("Start creating game with clients: {}", clientList);

        return new Thread(() -> {
            String uuid = UUID.randomUUID().toString();

            List<ClientDescription> clientsDescription = clientList.stream()
                    .map(client -> {
                        try {
                            return client.receiveMessage();
                        } catch (IOException | ClassNotFoundException e) {
                            log.error(UNPROCESSABLE_MESSAGE_FROM_CLIENT);
                            return ClientMessage.builder().build();
                        }
                    })
                    .map(ClientMessage::getClientDescription)
                    .collect(Collectors.toList());

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
                    .forEach(player -> getGameProcessThread(newGame.getGameDescription().getGameId()
                            , player).start());
        });
    }

    public Thread getGameProcessThread(String gameId, Client client) {
        return new Thread(() -> {
            try {
                while (true) {
                    if (!client.getClientSocket().isClosed()) {
                        // ход
                        ClientMessage message = client.receiveMessage();
                        log.debug("Receive message {} fom client {}", message, client.getDescription().getName());
                        // Optional.ofNullable(activeGames.get(gameId))
                        //         .ifPresent(game -> connectionService.broadcast(game.getPlayers(), message));
                    } else {
                        // дисконект, закрываем всех с ошибкой
                        ServerMessage message = ServerMessage.builder().message(ResultMessage.CONNECTION_ABORTED).build();
                        Optional.ofNullable(activeGames.get(gameId))
                                .ifPresent(game -> connectionService.broadcast(game.getPlayers(), message));
                        this.finishGame(gameId);
                    }

                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    public void finishGame(String gameId) {
        Optional.ofNullable(activeGames.get(gameId))
                .ifPresent((game -> {
                    game.getPlayers().forEach(p -> {
                        try {
                            p.stopConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }));
    }

    public static GameServiceImpl getInstance() {
        return instance;
    }

}
