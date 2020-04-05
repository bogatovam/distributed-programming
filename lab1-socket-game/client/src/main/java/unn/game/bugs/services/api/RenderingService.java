package unn.game.bugs.services.api;

import unn.game.bugs.models.Point;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;

import java.util.Map;

public interface RenderingService {
    void buildGameScene(GameDescription gameDescription, Map<String, ClientDescription> allClients, ClientDescription clientDescription);

    void buildErrorScene(final String errorMessage);

    Point getFieldPointByCanvasCoords(double x, double y);

    void drawGameField(GameDescription gameDescription, Map<String, ClientDescription> allClients);
}
