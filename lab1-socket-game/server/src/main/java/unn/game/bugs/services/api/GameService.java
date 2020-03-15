package unn.game.bugs.services.api;

import unn.game.bugs.models.Client;

import java.util.List;

public interface GameService {
    Thread createGame(List<Client> clientList);
}
