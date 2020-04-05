package unn.game.bugs.services.api;

import java.io.IOException;

public interface ServerManager  {
    void createAndStartServer(int port) throws IOException;

    void startGameSession();

    void stopServer() throws IOException;
}
