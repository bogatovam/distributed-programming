package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.message.ClientMessage;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;
import java.net.Socket;

import static unn.game.bugs.models.Constants.*;

@Slf4j
public class ConnectionServiceImpl implements ConnectionService {

    private final RenderingService renderingService = RenderingServiceImpl.getInstance();
    private static ConnectionServiceImpl instance = new ConnectionServiceImpl();

    private ConnectionServiceImpl() {}

    @Override
    public Client createConnection(String clientName) {
        try {
            Client client = new Client(new Socket("127.0.0.1", SERVER_PORT));
            this.processAfterConnection(client, clientName);
            return client;
        } catch (IOException e) {
            log.error(CREATE_CONNECTION_ERROR + ": " + e.getMessage());
            renderingService.buildErrorScene(CREATE_CONNECTION_ERROR);
            return null;
        }
    }

    protected void processAfterConnection(Client client, String clientName) {
        ClientDescription description = new ClientDescription(clientName);
        client.setClientDescription(description);
        client.sendMessage(ClientMessage.builder()
                                        .clientDescription(description)
                                        .build());
    }

    public static ConnectionServiceImpl getInstance() {
        return instance;
    }
}
