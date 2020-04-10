package unn.game.bugs.models.ui;

import lombok.Builder;
import lombok.Data;
import unn.game.bugs.models.Point;

import java.io.Serializable;

@Data
@Builder
public class Bug implements Serializable {
    private String setBy; // кем поставлен
    private String killBy;// кем раздавлен
    private boolean isBase;
    private Point point;
    
    public boolean isAlive() {
        return killBy == null;
    }
}
