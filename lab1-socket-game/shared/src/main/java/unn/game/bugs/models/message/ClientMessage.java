package unn.game.bugs.models.message;

import lombok.Builder;
import lombok.Data;
import unn.game.bugs.models.Point;
import unn.game.bugs.models.ui.ClientDescription;

import java.io.Serializable;

@Builder
@Data
public class ClientMessage implements Serializable {
    private ClientDescription clientDescription;
    private Point point;
}
