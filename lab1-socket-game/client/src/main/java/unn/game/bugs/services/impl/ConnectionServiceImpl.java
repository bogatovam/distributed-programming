package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.Client;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.RenderingService;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import static unn.game.bugs.models.Constants.SERVER_PORT;

@Slf4j
public class ConnectionServiceImpl implements ConnectionService {
    private final RenderingService renderingService = new RenderingServiceImpl();

    @Override
    public Client createConnection(String clientName) {
        Client client = null;
        try {
            client = new Client(new Socket("127.0.0.1", SERVER_PORT));
            this.processAfterConnection(client, clientName);
            return client;
        } catch (ConnectException e) {
            log.error("Error connecting to server: " + e.getMessage());
            renderingService.buildErrorScene("Error connecting to server: " + e.getMessage());
        } catch (IOException e) {
            log.error("Unprocessable message from server: " + e.getMessage());
            renderingService.buildErrorScene("Unprocessable message from server: " + e.getMessage());
        }
        return client;
    }

    protected void processAfterConnection(Client client, String clientName) throws ConnectException, IOException {
        ClientDescription description = new ClientDescription(clientName);
        client.setDescription(description);
        client.sendMessage(description);
    }
}
