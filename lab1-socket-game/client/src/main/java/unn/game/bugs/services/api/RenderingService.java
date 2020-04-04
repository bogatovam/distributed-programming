package unn.game.bugs.services.api;


public interface RenderingService {
    void buildGameScene();

    void buildErrorScene(final String errorMessage);
}
