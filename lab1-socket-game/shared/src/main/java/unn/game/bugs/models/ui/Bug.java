package unn.game.bugs.models.ui;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Bug implements Serializable {
    private String setBy; // кем поставлен
    private String killBy;// кем раздавлен
    private boolean isBase;

    public boolean isAlive() {
        return killBy == null;
    }
}
