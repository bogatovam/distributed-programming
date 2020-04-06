package unn.game.bugs.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Point implements Serializable {
    private int x;
    private int y;
}
