package unn.game.bugs.models.message;

import lombok.Builder;
import lombok.Data;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameField;

import java.util.List;

/* TODO add shared module*/
@Data
@Builder
public class ServerMessage {
    private ClientDescription clientDescription;
    private List<ClientDescription> allClients;
    private GameField field;
}
