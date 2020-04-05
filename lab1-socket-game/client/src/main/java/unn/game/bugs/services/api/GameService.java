package unn.game.bugs.services.api;

import unn.game.bugs.models.Client;

public interface GameService {
    void startGame(Client client);

    void stopGame();

    void skipMove();

    void makeMove(double x, double y);
}
