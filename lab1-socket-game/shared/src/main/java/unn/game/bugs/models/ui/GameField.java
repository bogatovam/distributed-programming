package unn.game.bugs.models.ui;

import lombok.Data;
import unn.game.bugs.models.ui.Bug;
import unn.game.bugs.models.ui.FieldCell;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static unn.game.bugs.models.Constants.*;

@Data
public class GameField {
    private FieldCell[][] field = new FieldCell[FIELD_SIZE_X][FIELD_SIZE_Y];

    public GameField(List<ClientDescription> clientDescriptionList) {
        this.generateField(clientDescriptionList);
    }

    private void generateField(List<ClientDescription> clientDescriptionList) {
        Random random = new Random();
        int step_x = FIELD_SIZE_X / clientDescriptionList.size();
        int step_y = FIELD_SIZE_Y / clientDescriptionList.size();
        for (int i = 0; i < clientDescriptionList.size(); ++i) {

            int x = (step_x * i) + random.nextInt(step_x);
            int y = (step_y * i) + random.nextInt(step_y);
            field[step_x][step_y].setCell(
                    Optional.of(
                            Bug.builder().setBy(clientDescriptionList.get(i).getId()).build()
                    )
            );
        }
    }
}
