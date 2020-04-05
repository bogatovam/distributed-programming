package unn.game.bugs.models.ui;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Color implements Serializable {
    private double r;
    private double g;
    private double b;
}
