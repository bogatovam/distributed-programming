package unn.game.bugs.models.ui;

import lombok.Data;

import java.util.Optional;

@Data
public class FieldCell {
    private Optional<Bug> cell = Optional.empty();
    private String componentId; // default  null
}
