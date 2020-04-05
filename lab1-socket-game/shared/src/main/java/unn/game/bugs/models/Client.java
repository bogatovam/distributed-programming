package unn.game.bugs.models;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.ui.ClientDescription;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

@Data
@Slf4j
public class Client {
    // что будет, если этот сокет одновременно используется в потоках разных игроков
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientDescription description;

    public Client(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;

        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public <T> void sendMessage(T objectMessage) throws ConnectException, IOException {
        if (out == null) {
            throw new ConnectException("There is no connection: socket is null");
        }
        out.writeObject(objectMessage);
    }

    public <T> T receiveMessage() throws ConnectException, IOException, ClassNotFoundException {
        if (in == null) {
            throw new ConnectException("There is no connection: socket is null");
        }
        return (T) in.readObject();
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
