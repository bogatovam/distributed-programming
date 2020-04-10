package unn.game.bugs.models.ui;

import lombok.Data;
import unn.game.bugs.models.Point;

import java.util.List;

@Data
public class Component {
    private String id;
    private String ownerId;
    private List<Point> connectedPoints;
    private List<Point> constituentPoints;
}
