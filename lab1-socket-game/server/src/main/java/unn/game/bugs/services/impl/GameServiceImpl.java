package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.Game;
import unn.game.bugs.services.api.GameService;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GameServiceImpl implements GameService {
    // здесь нет базы, чисто взамен нее
    private ConcurrentHashMap<String, Game> activeGames = new ConcurrentHashMap<>();

    @Override
    public Thread createGame(List<Client> clientList) {
        Game gameToCreate = new Game(clientList);
        return new Thread(() -> {
            String uuid = UUID.randomUUID().toString();
            activeGames.put(uuid, gameToCreate);

            gameToCreate.getPlayers()
                    .forEach(player -> {
                        try {
                            player.sendMessage(uuid);
                            getGameProcessThread(uuid, player).start();
                        } catch (ConnectException e) {
                            e.printStackTrace();
                        }
                    });
        });
    }

    public Thread getGameProcessThread(String gameId, Client client) {
        return new Thread(() -> {
            try {
                while (true) {
                    if (!client.getClientSocket().isClosed()) {
                        // ход
                        String message = client.receiveMessage();
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
                    game.getPlayers().forEach( p -> {
                        try {
                            p.stopConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }));
    }

    public void broadcast(String gameId, String message) {
        Optional.ofNullable(activeGames.get(gameId))
                .ifPresent((game -> {
                    log.trace("Broadcast message");
                    game.getPlayers().forEach(players -> {
                        try {
                            players.sendMessage(message);
                        } catch (ConnectException e) {
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
