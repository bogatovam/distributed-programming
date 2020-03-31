package unn.game.bugs.services.impl;

import lombok.EqualsAndHashCode;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.ServerManager;

import java.io.IOException;
import java.net.ServerSocket;

@EqualsAndHashCode
public class ServerManagerImpl implements ServerManager {
    private ServerSocket serverSocket;
    private ConnectionService connectionService = new ConnectionServiceImpl();

    @Override
    public void createAndStartServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            connectionService.addClient(serverSocket.accept());
        }
    }

    @Override
    public void stopServer() throws IOException {
        serverSocket.close();
    }
}
