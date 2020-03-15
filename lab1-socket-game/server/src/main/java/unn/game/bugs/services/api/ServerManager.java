package unn.game.bugs.services.api;

import java.io.IOException;

public interface ServerManager {
    void createAndStartServer(int port) throws IOException;

    void stopServer() throws IOException;
}
