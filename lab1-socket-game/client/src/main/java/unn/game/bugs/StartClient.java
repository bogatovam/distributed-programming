package unn.game.bugs;

import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.impl.GameServiceImpl;

import java.io.IOException;

public class StartClient {
    public static void main(String[] args) throws IOException {
        GameService client = new GameServiceImpl();
        client.startConnection("127.0.0.1", 8080);
        String response = client.sendMessage("hello server");
        System.out.println(response);
    }
}