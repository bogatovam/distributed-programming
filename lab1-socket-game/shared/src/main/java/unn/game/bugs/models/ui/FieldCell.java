package unn.game.bugs.models.ui;

import lombok.Data;

import java.io.Serializable;

@Data
public class FieldCell implements Serializable {
    private Bug bug;            // default  null
    private String componentId; // default  null

    public boolean isEmpty() {
        return bug == null;
    }

}
