package unn.game.bugs.services.api;

import javafx.scene.paint.Color;
import unn.game.bugs.models.Point;
import unn.game.bugs.models.ui.ClientDescription;
import unn.game.bugs.models.ui.GameDescription;

import java.util.Map;

public interface RenderingService {
    void buildGameScene(GameDescription gameDescription, Map<String, ClientDescription> allClients, ClientDescription clientDescription);

    void buildErrorScene(final String errorMessage);

    Point getFieldPointByCanvasCoords(double x, double y);

    void drawGameField(GameDescription gameDescription, Map<String, ClientDescription> allClients);

    void drawBugCell(double x, double y, double w, double h, Color color);
}
