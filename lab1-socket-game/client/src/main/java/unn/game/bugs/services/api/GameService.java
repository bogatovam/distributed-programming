package unn.game.bugs.services.api;

import java.io.IOException;

public interface GameService {
    void stopConnection() throws IOException;

    String sendMessage(String msg) throws IOException;

    void startConnection(String ip, int port) throws IOException;
}
