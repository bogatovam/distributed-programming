package unn.game.bugs.models;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.models.ui.ClientDescription;

import java.io.*;
import java.net.Socket;

import static unn.game.bugs.models.Constants.*;

@Data
@Slf4j
public class Client {
    // что будет, если этот сокет одновременно используется в потоках разных игроков
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ClientDescription description;

    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;

        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
           log.error(OBJECT_STREAM_ERROR + ": " +  e.getMessage());
        }
    }

    public <T> void sendMessage(T objectMessage) {
        try {
            out.writeObject(objectMessage);
        } catch (IOException e) {
            log.error(UNPROCESSABLE_SENDING_MESSAGE + ": " +  e.getMessage());
        }
    }

    public <T> T receiveMessage() {
        try {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(UNPROCESSABLE_RECEIVING_MESSAGE + ": " +  e.getMessage());
        }
        return null;
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            log.error(CLOSE_CONNECTION_ERROR + ": " +  e.getMessage());
        }
    }
}
