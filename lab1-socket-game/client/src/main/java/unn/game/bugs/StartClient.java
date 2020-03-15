package unn.game.bugs;

import unn.game.bugs.services.api.GameService;
import unn.game.bugs.services.impl.GameServiceImpl;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class StartClient {
    public static void main(String[] args) throws IOException {
        GameService client = new GameServiceImpl();
        client.startConnection("127.0.0.1", 8080);
        Integer myNum = ThreadLocalRandom.current().nextInt(0, 10);
        String response = client.sendMessage("name" + myNum.toString());
        System.out.println(response);
        client.sendMessage("name" + myNum.toString());
    }
}