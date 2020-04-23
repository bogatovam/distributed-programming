package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Constants;
import unn.game.bugs.models.Client;
import unn.game.bugs.services.api.ConnectionService;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConnectionServiceImpl implements ConnectionService {

    private List<Client> pendingClients = new ArrayList<>();

    private static ConnectionServiceImpl instance = new ConnectionServiceImpl();

    private ConnectionServiceImpl() {}

    @Override
    public boolean addClient(Socket clientSocket) throws IOException {
        // обрабатывать в отдельном потоке одно условие не обязательно: это
        // лишнее усложнение в данном случае
        // если от клиентов необходимо прочитать какое то "приветственное
        // сообщение", то тогда придется
        pendingClients.add(new Client(clientSocket));
        log.debug("Сокет был добавлен в список ожидания");
        if (pendingClients.size() == Constants.PLAYERS_COUNT) {
            return true;
        }
        return false;
    }

    @Override
    public <T> void broadcast(List<Client> clients, T message) {
        log.debug("Рассылка сообщения {} среди клиентов {}", message, clients);
        clients.stream()
               .filter(client -> client.getClientDescription()
                                        .isActive())
               .forEach(client -> {
                   client.sendMessage(message);
               });
    }

    @Override
    public List<Client> getPendingClientsAndClear() {
        List<Client> clientsForNewGame = new ArrayList<>(this.pendingClients);
        pendingClients.clear();
        return clientsForNewGame;
    }

    public static ConnectionServiceImpl getInstance() {
        return instance;
    }
}
