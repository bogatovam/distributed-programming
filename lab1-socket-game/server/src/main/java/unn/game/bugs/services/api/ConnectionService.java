package unn.game.bugs.services.api;

import unn.game.bugs.models.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public interface ConnectionService {
    boolean addClient(Socket clientSocket) throws IOException;

    <T> void broadcast(List<Client> clients, T message);

    List<Client> getPendingClientsAndClear();
}
