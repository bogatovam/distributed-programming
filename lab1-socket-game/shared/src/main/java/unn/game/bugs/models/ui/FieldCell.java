package unn.game.bugs.models.ui;

import lombok.Data;

import java.io.Serializable;
import java.util.Optional;

@Data
public class FieldCell implements Serializable {
    private Optional<Bug> cell = Optional.empty();
    private String componentId; // default  null
}
