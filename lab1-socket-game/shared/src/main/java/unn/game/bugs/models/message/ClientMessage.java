package unn.game.bugs.models.message;

import unn.game.bugs.models.Point;

import java.io.Serializable;

public class ClientMessage implements Serializable {
    private String clientId;
    private Point point;
}
