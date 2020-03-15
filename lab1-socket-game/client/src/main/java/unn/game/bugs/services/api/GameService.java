package unn.game.bugs.services.api;

import java.io.IOException;

public interface GameService {
    void stopConnection() throws IOException;

    void sendMessage(String msg) throws IOException;

    String receiveMessage() throws IOException;

    void startConnection(String ip, int port) throws IOException;
}
