package unn.game.bugs.models.ui;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class GameDescription implements Serializable {
    private String gameId;
    private FieldCell[][] field;
    private String currentPlayerId;
    private int currentPlayerNumber = 0;
    private int remainingMoves;
}
