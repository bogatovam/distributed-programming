package unn.game.bugs.services.api;

import java.io.IOException;

public interface ConnectionService {
    void createConnection();

    void stopConnection() throws IOException;

    void sendMessage(String msg) throws IOException;

    String receiveMessage() throws IOException;
}
