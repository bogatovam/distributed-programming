package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.message.ClientMessage;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import static unn.game.bugs.models.Constants.*;

@Slf4j
public class ConnectionServiceImpl implements ConnectionService {
    private final RenderingService renderingService = RenderingServiceImpl.getInstance();
    private static ConnectionServiceImpl instance = new ConnectionServiceImpl();

    private ConnectionServiceImpl() {
    }

    @Override
    public Client createConnection(String clientName) {
        Client client = null;
        try {
            client = new Client(new Socket("127.0.0.1", SERVER_PORT));
            this.processAfterConnection(client, clientName);
            return client;
        } catch (ConnectException e) {
            log.error(SERVER_CONNECTION_ERROR + ": " + e.getMessage());
            renderingService.buildErrorScene(SERVER_CONNECTION_ERROR);
        } catch (IOException e) {
            log.error(UNPROCESSABLE_MESSAGE_FROM_SERVER + ": " + e.getMessage());
            renderingService.buildErrorScene(UNPROCESSABLE_MESSAGE_FROM_SERVER);
        }
        return client;
    }

    protected void processAfterConnection(Client client, String clientName) throws ConnectException, IOException {
        ClientDescription description = new ClientDescription(clientName);
        client.setDescription(description);
        client.sendMessage(ClientMessage.builder().clientDescription(description).build());
    }

    public static ConnectionServiceImpl getInstance() {
        return instance;
    }
}
