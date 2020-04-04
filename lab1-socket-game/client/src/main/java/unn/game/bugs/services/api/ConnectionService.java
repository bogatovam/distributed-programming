package unn.game.bugs.services.api;

import unn.game.bugs.models.Client;

public interface ConnectionService {
    Client createConnection(String clientName);
}
