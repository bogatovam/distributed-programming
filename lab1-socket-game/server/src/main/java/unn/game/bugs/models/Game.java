package unn.game.bugs.models;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
public class Game {
    private List<Client> players;

    public Game(List<Client> players) {
        this.players = new ArrayList<>();
        this.players.addAll(players);
    }

    public List<Client> getPlayers() {
        return players;
    }
}
