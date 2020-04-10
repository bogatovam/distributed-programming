package unn.game.bugs.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode
public class Component {
    private String id;
    private String owner;
    private List<Point> aliveBugsAround;
}
