package unn.game.bugs.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import unn.game.bugs.models.ui.GameDescription;

import java.util.List;
import java.util.Map;

@Builder
@Data
@EqualsAndHashCode
public class Game {
    private List<Client> players;
    private GameDescription gameDescription;
    private Map<String, Component> allComponents;
}
