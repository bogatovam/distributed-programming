package unn.game.bugs.services.api;

import java.io.IOException;
import java.net.Socket;

public interface ConnectionService {
    void addPlayer();

    void deletePlayer();

    void startGameSession();

    void deleteGameSession();

    void addClient(Socket clientSocket) throws IOException;
}
