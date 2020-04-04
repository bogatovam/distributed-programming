package unn.game.bugs.models;

import lombok.Data;
import unn.game.bugs.models.ui.ClientDescription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

@Data
public class Client {
    // что будет, если этот сокет одновременно используется в потоках разных игроков
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private ClientDescription description;

    public Client(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(String msg) throws ConnectException {
        if (out == null) {
            throw new ConnectException("There is no connection: socket is null");
        }
        out.println(msg);
    }

    public String receiveMessage() throws IOException {
        if (in == null) {
            throw new ConnectException("There is no connection: socket is null");
        }
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        if (in == null || out == null || clientSocket == null) {
            throw new ConnectException("There is no connection: socket is null");
        }
        in.close();
        out.close();
        clientSocket.close();
    }
}
