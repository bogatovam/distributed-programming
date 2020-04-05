package unn.game.bugs.services.api;


import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;

import java.util.List;

public interface RenderingService {
    void buildGameScene(GameDescription gameDescription, List<ClientDescription> allClients, ClientDescription clientDescription);

    void buildErrorScene(final String errorMessage);
}
