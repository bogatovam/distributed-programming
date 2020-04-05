package unn.game.bugs.models.message;

import lombok.Builder;
import lombok.Data;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ServerMessage implements Serializable {
    private ResultMessage message;
    private Map<String, ClientDescription> allClients;
    private GameDescription gameDescription;
}
