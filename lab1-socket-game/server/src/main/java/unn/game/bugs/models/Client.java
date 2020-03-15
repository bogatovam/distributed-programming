package unn.game.bugs.models;

import lombok.EqualsAndHashCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

@EqualsAndHashCode
public class Client {
    // что будет, если этот сокет одновременно используется в потоках разных игроков
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String name;

    private static Logger log = Logger.getLogger(Client.class.getName());

    public Client(Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.name = readMessageFromSocket();
        log.info("client was added in pending list. Name: " + this.name);
    }

    public String readMessageFromSocket() throws IOException {
        return in.readLine();
    }

    public void sendMessageBySocket(String message) {
        out.println("some message");
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            clientSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
