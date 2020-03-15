package unn.game.bugs.services.impl;

import unn.game.bugs.models.Client;
import unn.game.bugs.models.Game;
import unn.game.bugs.services.api.GameService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class GameServiceImpl implements GameService {
    // здесь нет базы, чисто взамен нее
    private ConcurrentHashMap<String, Game> activeGames = new ConcurrentHashMap<>();

    private static Logger log = Logger.getLogger(GameServiceImpl.class.getName());

    @Override
    public Thread createGame(List<Client> clientList) {
        Game gameToCreate = new Game(clientList);
        return new Thread(() -> {
            String uuid = UUID.randomUUID().toString();
            activeGames.put(uuid, gameToCreate);

            gameToCreate.getPlayers()
                    .forEach(player -> {
                        player.sendMessageBySocket(uuid);
                        getGameProcessThread(uuid, player).start();
                    });
        });
    }

    public Thread getGameProcessThread(String gameId, Client client) {
        return new Thread(() -> {
            try {
                while (true) {
                    if (!client.getClientSocket().isClosed()) {
                        // ход
                        String message = client.readMessageFromSocket();
                        this.broadcast(gameId, message);
                    } else {
                        // дисконект, закрываем все с ошибкой
                        this.broadcast(gameId, "error");
                        this.finishGame(gameId);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void finishGame(String gameId) {
        Optional.ofNullable(activeGames.get(gameId))
                .ifPresent((game -> {
                    game.getPlayers().forEach(Client::closeConnection);
                }));
    }

    public void broadcast(String gameId, String message) {
        Optional.ofNullable(activeGames.get(gameId))
                .ifPresent((game -> {
                    log.info("Try to brodcasting send message");
                    game.getPlayers().forEach(players -> {
                        players.sendMessageBySocket(message);
                    });
                }));
    }
}
