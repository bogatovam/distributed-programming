package unn.game.bugs.models.ui;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Bug {
    private String setBy; // кем поставлен
    private String killBy;// кем раздавлен
    private boolean isBase;

    public boolean isAlive() {
        return killBy == null;
    }
}
