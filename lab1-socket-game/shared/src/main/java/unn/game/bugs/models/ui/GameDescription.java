package unn.game.bugs.models.ui;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;

import static unn.game.bugs.models.Constants.*;

@Data
public class GameDescription implements Serializable {
    private String gameId;
    private FieldCell[][] field = new FieldCell[FIELD_SIZE_X][FIELD_SIZE_Y];

    public GameDescription(final String gameId, Map<String, ClientDescription> clientDescriptionList) {
        this.gameId = gameId;
        this.generateField(clientDescriptionList);
    }

    private void generateField(Map<String, ClientDescription> clientDescriptionMap) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                field[i][j] = new FieldCell();
            }
        }

        Random random = new Random();
        int step_x = FIELD_SIZE_X / clientDescriptionMap.size();
        int step_y = FIELD_SIZE_Y / clientDescriptionMap.size();

        int i = 0;
        for (Map.Entry<String, ClientDescription> entry : clientDescriptionMap.entrySet()) {
            int x = (step_x * i) + random.nextInt(step_x);
            int y = (step_y * i) + random.nextInt(step_y);
            field[x][y].setBug(
                    Bug.builder().setBy(entry.getKey()).build()
            );
            i++;
        }
    }
}
