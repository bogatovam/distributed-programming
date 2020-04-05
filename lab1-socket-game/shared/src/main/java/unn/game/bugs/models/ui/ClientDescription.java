package unn.game.bugs.models.ui;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class ClientDescription implements Serializable {
    private String id;
    private String name;

    public ClientDescription(final String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }
}
