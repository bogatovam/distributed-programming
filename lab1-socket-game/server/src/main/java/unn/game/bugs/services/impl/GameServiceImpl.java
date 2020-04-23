package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.Component;
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

import static unn.game.bugs.models.Constants.*;

@Slf4j
public class GameServiceImpl implements GameService {

    // здесь нет базы, это взамен нее
    private Map<String, Game> activeGames = new ConcurrentHashMap<>();
    private final ConnectionService connectionService = ConnectionServiceImpl.getInstance();

    private static GameServiceImpl instance = new GameServiceImpl();

    private GameServiceImpl() {}

    @Override
    public Thread createGame(List<Client> clientList) {
        log.debug("Start creating game with clients: {}", clientList);

        return new Thread(() -> {
            String uuid = UUID.randomUUID()
                              .toString();

            List<Client> clientListWithDescription = clientList.stream()
                                                               .peek(client -> {
                                                                   ClientMessage message = client.receiveMessage();
                                                                   client.setClientDescription(message.getClientDescription());
                                                               })
                                                               .collect(Collectors.toList());

            Map<String, ClientDescription> clientsDescription = clientListWithDescription.stream()
                                                                                         .map(Client::getClientDescription)
                                                                                         .collect(Collectors.toMap(ClientDescription::getId,
                                                                                                                   Function.identity()));

            Map<String, List<Point>> clientsAliveBugs = new LinkedHashMap<>();

            clientListWithDescription.forEach(client -> {
                clientsAliveBugs.put(client.getClientDescription()
                                           .getId(),
                                     new LinkedList<>());
            });
            Game newGame = Game.builder()
                               .clientsAliveBugs(clientsAliveBugs)
                               .gameDescription(GameDescription.builder()
                                                               .gameId(uuid)
                                                               .field(generateField(clientsDescription,
                                                                                    clientsAliveBugs))
                                                               .currentPlayerId(clientListWithDescription.get(0)
                                                                                                         .getClientDescription()
                                                                                                         .getId())
                                                               .remainingMoves(MOVES)
                                                               .build())
                               .players(clientListWithDescription)
                               .allComponents(new LinkedHashMap<>())
                               .build();

            // Сохранить игру в список всех активных игр
            activeGames.put(uuid, newGame);
            // send init game to all client
            ServerMessage initMessage = ServerMessage.builder()
                                                     .message(ResultMessage.CONNECTION_SUCCESSFUL)
                                                     .allClients(clientsDescription)
                                                     .gameDescription(newGame.getGameDescription())
                                                     .build();
            // Послать сообщение для инициализации всем клиентам
            connectionService.broadcast(clientList, initMessage);

            newGame.getPlayers()
                   .forEach(player -> getGameProcessThread(newGame.getGameDescription()
                                                                  .getGameId(),
                                                           player).start());
        });
    }

    public Thread getGameProcessThread(String gameId, Client client) {
        return new Thread(() -> {
            while (true) {
                if (!client.getClientSocket()
                           .isClosed()) {
                    // ход
                    ClientMessage message = client.receiveMessage();
                    log.debug("Receive message {} from client {}", message, client.getClientDescription()
                                                                                  .getName());
                    // обработка хода должна быть синхронизированной
                    // словарь activeGames хранит актуальное(!) состояние каждой
                    // игры
                    // Любое изменение должно фиксироваться в gameDescription
                    // соотвествтующего объекта
                    this.processMove(gameId, message);
                } else {
                    // дисконект, закрываем всех с ошибкой
                    ServerMessage message = ServerMessage.builder()
                                                         .message(ResultMessage.CONNECTION_ABORTED)
                                                         .build();
                    connectionService.broadcast(activeGames.get(gameId)
                                                           .getPlayers(),
                                                message);
                    this.finishGame(activeGames.get(gameId));
                }
            }
        });
    }

    public void finishGame(Game game) {
        game.getPlayers()
            .stream()
            .filter(p -> !p.getClientSocket()
                           .isClosed())
            .forEach(Client::stopConnection);
    }

    public synchronized void processMove(String gameId, ClientMessage clientMessage) {
        // Также делает броадкаст, потому что броадкаст тоже должен быть
        // выполнен синхрнизированно
        Game game = activeGames.get(gameId);

        log.debug("Проверяем очерпедность хода");
        if (!game.getGameDescription()
                 .getCurrentPlayerId()
                 .equals(clientMessage.getClientDescription()
                                      .getId())) {
            game.getPlayers()
                .stream()
                .filter(player -> player.getClientDescription()
                                        .getId()
                                        .equals(clientMessage.getClientDescription()
                                                             .getId()))
                .forEach(client -> client.sendMessage(this.getActionProhibitedMessage(game)));
            return;
        }

        FieldCell[][] field = game.getGameDescription()
                                  .getField();

        Point point = clientMessage.getPoint();
        int x = point.getX();
        int y = point.getY();

        String clientId = clientMessage.getClientDescription()
                                       .getId();

        // Есть ли кто то мертвый в этой точке? Или может там я?
        log.debug("Есть ли кто то мертвый в этой точке? Или может там я?");
        if (field[x][y].getBug() != null && (!field[x][y].getBug()
                                                         .isAlive()
                || field[x][y].getBug()
                              .getSetBy()
                              .equals(clientId))) {
            // ход сделать нельзя
            log.debug("Здесь кто то умер / или уже стоит мой жук");
            connectionService.broadcast(game.getPlayers(), getActionProhibitedMessage(game));
        } else {
            List<FieldCell> aliveAround = getAllClientAliveBugsAround(field, point, clientId);
            List<FieldCell> allKilledByClientAround = getAllKilledByClientBugsAround(field, point, clientId);
            List<Component> componentsAround = allKilledByClientAround.stream()
                                                                      .map(bugInCell -> game.getAllComponents()
                                                                                            .get(bugInCell.getComponentId()))
                                                                      .collect(Collectors.toList());

            List<Component> connectedComponents = componentsAround.stream()
                                                                  // Является ли
                                                                  // компонента
                                                                  // подключенной:
                                                                  // есть ли
                                                                  // вокруг нее
                                                                  // живые жуки
                                                                  // клиента
                                                                  .filter(componentInCell -> !componentInCell.getAliveBugsAround()
                                                                                                             .isEmpty())
                                                                  .collect(Collectors.toList());

            // есть ли рядом со мной живые клопы?
            log.debug(" Ок, клетка потенциально свободна. Есть ли рядом со мной мои живые клопы?");
            if (!aliveAround.isEmpty()) {
                log.debug("Рядом есть мои живые, отлично. Можно сделать ход");
                // сделать ход
                broadcastMessageOrEndGame(game,
                                          makeMoveAndGetMessage(game, point, clientId, componentsAround, aliveAround));
            } else {
                log.debug("Рядом живых нет :( Проверим, есть ли те, кого я убивал");
                if (!allKilledByClientAround.isEmpty()) {
                    log.debug("Есть те, кого я убивал. Проверим, могу ли я рядом с ними встать (подключенность)");

                    // если есть есть хотя бы одна подклченная компонента рядом,
                    // то можем сделать ход
                    if (!connectedComponents.isEmpty()) {
                        // сделать ход
                        log.debug("Ок, рядом подсключенная компонента: {}. Делаем ход", connectedComponents);
                        broadcastMessageOrEndGame(game, makeMoveAndGetMessage(game, point, clientId, componentsAround,
                                                                              aliveAround));
                    } else {
                        log.debug("Нет, рядом компонента неподключенная. Ход сделать нельзя");
                        // ход сделать нельзя
                        connectionService.broadcast(game.getPlayers(), getActionProhibitedMessage(game));
                    }
                } else {
                    log.debug("Нет, я никого не убивал. Ход сделать нельзя");
                    // ход сделать нельзя
                    connectionService.broadcast(game.getPlayers(), getActionProhibitedMessage(game));
                }
            }
        }
    }

    private ServerMessage makeMoveAndGetMessage(Game game, Point point, String clientId,
            List<Component> componentsAround, List<FieldCell> aliveAround) {
        FieldCell[][] field = game.getGameDescription()
                                  .getField();

        int x = point.getX();
        int y = point.getY();

        if (field[x][y].isEmpty()) {
            log.debug("Клетка пустая: ставим в нее жука");
            setBugAtPoint(point, clientId, field);
            game.getClientsAliveBugs()
                .get(clientId)
                .add(point);
            log.debug("Обновляем прилигающие компоненты, если таковые есть");

            componentsAround.stream()
                            .map(component -> {
                                List<Point> activeBugs = component.getAliveBugsAround();
                                activeBugs.add(point);
                                component.setAliveBugsAround(activeBugs);
                                return component;
                            })
                            .forEach(component -> game.getAllComponents()
                                                      .merge(component.getId(), component, (v1, v2) -> v2));

        } else {
            log.debug("Убиываем");

            log.debug("Есть ли рядом компоненты убиваемого?");
            List<FieldCell> componentsKilledBug = getAllKilledByClientBugsAround(field, point, field[x][y].getBug()
                                                                                                          .getSetBy());

            if (!componentsKilledBug.isEmpty()) {
                log.debug("Компоненты есть, убираем у них текущую точку из активных");

                List<Component> affectedComponents = componentsKilledBug.stream()
                                                                        .map(c -> game.getAllComponents()
                                                                                      .get(c.getComponentId()))
                                                                        .collect(Collectors.toList());

                affectedComponents.stream()
                                  .map(component -> {
                                      List<Point> activeBugs = component.getAliveBugsAround();
                                      activeBugs.remove(point);
                                      component.setAliveBugsAround(activeBugs);
                                      log.debug("Точка {} была удалена из {}", point, component.getId());
                                      return component;
                                  })
                                  .forEach(component -> game.getAllComponents()
                                                            .merge(component.getId(), component, (v1, v2) -> v2));
            }
            log.debug("Расширим текущую компоненту или создадим новую");

            Optional<Component> nearestComponent = componentsAround.stream()
                                                                   .findFirst();
            if (nearestComponent.isPresent()) {
                Component component = nearestComponent.get();
                killBugAtPoint(point, clientId, component.getId(), field);
                game.getClientsAliveBugs()
                    .get(field[x][y].getBug()
                                    .getSetBy())
                    .remove(point);
                log.debug("Текущаяя компонента была расширена: {}", nearestComponent);
            } else {

                Component newComponent = Component.builder()
                                                  .id(UUID.randomUUID()
                                                          .toString())
                                                  .aliveBugsAround(aliveAround.stream()
                                                                              .map(c -> c.getBug()
                                                                                         .getPoint())
                                                                              .collect(Collectors.toList()))
                                                  .owner(clientId)
                                                  .build();
                log.debug("Создадана новая компонента: {}", newComponent);

                game.getAllComponents()
                    .put(newComponent.getId(), newComponent);
                killBugAtPoint(point, clientId, newComponent.getId(), field);
                game.getClientsAliveBugs()
                    .get(field[x][y].getBug()
                                    .getSetBy())
                    .remove(point);
            }
        }



        if (game.getGameDescription()
                .getRemainingMoves() == 1) {

            int nextPlayer = (game.getGameDescription()
                                  .getCurrentPlayerNumber()
                    + 1) % game.getPlayers()
                               .size();
            log.debug("У игрока закончились ходы. Ход переходит к следующему");
            game.getGameDescription()
                .setRemainingMoves(MOVES);

            game.getGameDescription()
                .setCurrentPlayerNumber(nextPlayer);

            game.getGameDescription()
                .setCurrentPlayerId(game.getPlayers()
                                        .get(nextPlayer)
                                        .getClientDescription()
                                        .getId());
        } else {
            game.getGameDescription()
                .setRemainingMoves(game.getGameDescription()
                                       .getRemainingMoves()
                        - 1);
        }
        log.debug("Инкремент игрового счетчика: у игрока {} осталось ходов {}", clientId, game.getGameDescription()
                                                                                              .getRemainingMoves());

        activeGames.replace(game.getGameDescription()
                                .getGameId(),
                            game);

        return ServerMessage.builder()
                            .message(ResultMessage.ACTION_APPLIED)
                            .allClients(getClientDescriptionFromGame(game))
                            .gameDescription(game.getGameDescription())
                            .build();
    }

    private List<FieldCell> getAllClientAliveBugsAround(FieldCell[][] field, Point point, String clientId) {
        List<FieldCell> bugs = new LinkedList<>();

        int upperBoundX = Math.min(point.getX() + 1, field[0].length);
        int lowerBoundX = Math.max(point.getX() - 1, 0);
        int upperBoundY = Math.min(point.getY() + 1, field.length);
        int lowerBoundY = Math.max(point.getY() - 1, 0);

        for (int x = lowerBoundX; x <= upperBoundX; x++) {
            for (int y = lowerBoundY; y <= upperBoundY; y++) {
                if (point.getX() == x && point.getY() == y) {
                    continue;
                }
                if (!field[x][y].isEmpty() && field[x][y].getBug()
                                                         .isAlive()
                        && clientId.equals(field[x][y].getBug()
                                                      .getSetBy())) {
                    bugs.add(field[x][y]);
                }
            }
        }
        return bugs;
    }

    private List<FieldCell> getAllKilledByClientBugsAround(FieldCell[][] field, Point point, String clientId) {
        List<FieldCell> bugs = new LinkedList<>();

        int upperBoundX = Math.min(point.getX() + 1, field[0].length);
        int lowerBoundX = Math.max(point.getX() - 1, 0);
        int upperBoundY = Math.min(point.getY() + 1, field.length);
        int lowerBoundY = Math.max(point.getY() - 1, 0);

        for (int x = lowerBoundX; x <= upperBoundX; x++) {
            for (int y = lowerBoundY; y <= upperBoundY; y++) {
                if (point.getX() == x && point.getY() == y) {
                    continue;
                }
                if (!field[x][y].isEmpty() && !field[x][y].getBug()
                                                          .isAlive()
                        && clientId.equals(field[x][y].getBug()
                                                      .getKillBy())) {
                    bugs.add(field[x][y]);
                }
            }
        }
        return bugs;
    }

    private boolean areThereEmptyCellsAround(FieldCell[][] field, Point point, String clientId) {
        List<FieldCell> cells = new LinkedList<>();

        int upperBoundX = Math.min(point.getX() + 1, field[0].length - 1);
        int lowerBoundX = Math.max(point.getX() - 1, 0);
        int upperBoundY = Math.min(point.getY() + 1, field.length - 1);
        int lowerBoundY = Math.max(point.getY() - 1, 0);

        for (int x = lowerBoundX; x <= upperBoundX; x++) {
            for (int y = lowerBoundY; y <= upperBoundY; y++) {
                if (point.getX() == x && point.getY() == y) {
                    continue;
                }
                if (field[x][y].isEmpty()) {
                    cells.add(field[x][y]);
                }
            }
        }
        return !cells.isEmpty();
    }

    public void broadcastMessageOrEndGame(Game game, ServerMessage message) {
        List<Client> losers = getLosersListIfExist(game);
        log.debug("Проверим, не привел ли ход текущего игрока к проигрышу кого нибудь");
        if (losers.isEmpty()) {
            log.debug("Все нормально: никто не проиграл");
            this.connectionService.broadcast(game.getPlayers(), message);
        } else {
            log.debug("Тут есть проигравшие: заканчиваем игру для них");
            this.connectionService.broadcast(losers, getLoseMessage(game));

            game.setPlayers(game.getPlayers()
                                .stream()
                                .map(client -> {
                                    if (client.getClientDescription()
                                              .isActive()) {

                                        client.getClientDescription()
                                              .setActive(!losers.contains(client));
                                    }
                                    return client;
                                })
                                .collect(Collectors.toList()));
            if (game.getActivePlayers()
                    .size() == 1) {
                game.getPlayers()
                    .get(0)
                    .sendMessage(getWinMessage(game));
            } else {
                this.connectionService.broadcast(game.getPlayers(), message);
            }
        }
    }

    public List<Client> getLosersListIfExist(Game game) {
        // проигравшим считется игрок, который не может сделать ход
        // то есть всех его жуков убили или они заблокированы
        FieldCell[][] field = game.getGameDescription()
                                  .getField();

        return game.getPlayers()
                   .stream()
                   .filter(client -> client.getClientDescription()
                                           .isActive())
                   .filter(client -> {
                       List<Point> aliveBugs = game.getClientsAliveBugs()
                                                   .get(client.getClientDescription()
                                                              .getId());
                       return aliveBugs.isEmpty() || aliveBugs.stream()
                                                              .noneMatch(point -> areThereEmptyCellsAround(field, point,
                                                                                                           client.getClientDescription()
                                                                                                                 .getId()));
                   })
                   .collect(Collectors.toList());

    }

    private Map<String, ClientDescription> getClientDescriptionFromGame(Game game) {
        return game.getPlayers()
                   .stream()
                   .map(Client::getClientDescription)
                   .collect(Collectors.toMap(ClientDescription::getId, Function.identity()));
    }

    private ServerMessage getActionProhibitedMessage(Game game) {
        return ServerMessage.builder()
                            .message(ResultMessage.ACTION_PROHIBITED)
                            .allClients(getClientDescriptionFromGame(game))
                            .gameDescription(game.getGameDescription())
                            .build();
    }

    private ServerMessage getLoseMessage(Game game) {
        return ServerMessage.builder()
                            .message(ResultMessage.LOSE)
                            .allClients(getClientDescriptionFromGame(game))
                            .gameDescription(game.getGameDescription())
                            .build();
    }

    private ServerMessage getWinMessage(Game game) {
        return ServerMessage.builder()
                            .message(ResultMessage.WIN)
                            .allClients(getClientDescriptionFromGame(game))
                            .gameDescription(game.getGameDescription())
                            .build();
    }

    public static FieldCell[][] generateField(Map<String, ClientDescription> clientDescriptionMap,
            Map<String, List<Point>> aliveBugs) {
        FieldCell[][] field = new FieldCell[FIELD_SIZE_X][FIELD_SIZE_Y];
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
            Point point = new Point(x, y);
            field[x][y].setBug(Bug.builder()
                                  .setBy(entry.getKey())
                                  .point(point)
                                  .build());
            aliveBugs.get(entry.getKey())
                     .add(point);
            i++;
        }

        return field;
    }

    public static FieldCell[][] setBugAtPoint(Point point, final String clientId, FieldCell[][] field) {
        field[point.getX()][point.getY()].setBug(Bug.builder()
                                                    .setBy(clientId)
                                                    .point(point)
                                                    .build());
        return field;
    }

    public static FieldCell[][] killBugAtPoint(Point point, final String clientId, String componentId,
            FieldCell[][] field) {
        Bug killed = field[point.getX()][point.getY()].getBug();
        killed.setKillBy(clientId);
        field[point.getX()][point.getY()].setBug(killed);
        field[point.getX()][point.getY()].setComponentId(componentId);
        log.debug("Killed bug at {}", field[point.getX()][point.getY()]);
        return field;
    }

    public static GameServiceImpl getInstance() {
        return instance;
    }

}
