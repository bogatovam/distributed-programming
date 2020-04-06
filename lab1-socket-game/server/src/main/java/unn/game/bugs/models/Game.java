package unn.game.bugs.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import unn.game.bugs.models.ui.Bug;
import unn.game.bugs.models.ui.FieldCell;
import unn.game.bugs.models.ui.GameDescription;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode
public class Game {
    private List<Client> players;
    private GameDescription gameDescription;

    public void setBugAtPoint(Point point, final String clientId) {
        FieldCell[][] field = new FieldCell[2][2];
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                field[i][j] = new FieldCell();
            }
        }
//        this.gameDescription.setField(null);
//        field[point.getX()][point.getY()].setBug(
//                Bug.builder().setBy(clientId).build()
//        );
//        gameDescription.setField(field);
    }
}
