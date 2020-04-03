package unn.game.bugs.services.api;

import java.io.IOException;

public interface RenderingService {
    void buildGameScene();

    void buildErrorScene(final String errorMessage);
}
