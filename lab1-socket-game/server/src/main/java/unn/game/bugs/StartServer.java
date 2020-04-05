package unn.game.bugs;

import unn.game.bugs.models.Constants;
import unn.game.bugs.services.api.ServerManager;
import unn.game.bugs.services.impl.ServerManagerImpl;

import java.io.IOException;

public class StartServer {
    public static void main(String[] args) throws IOException {
        ServerManager serverManager = ServerManagerImpl.getInstance();
        serverManager.createAndStartServer(Constants.SERVER_PORT);
    }
}
