package unn.game.bugs.services.impl;

import lombok.extern.slf4j.Slf4j;
import unn.game.bugs.services.api.ConnectionService;
import unn.game.bugs.services.api.RenderingService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

@Slf4j
public class ConnectionServiceImpl implements ConnectionService {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private final RenderingService renderingService = new RenderingServiceImpl();

    @Override
    public void createConnection() {
        // build client
        //
        try {
            this.startConnection("127.0.0.1", 8080);
        } catch (ConnectException e) {
            log.error("Error connecting to server: " + e.getMessage());
            renderingService.buildErrorScene("Error connecting to server: " + e.getMessage());
        } catch (IOException e) {
            log.error("Unprocessable message from server: " + e.getMessage());
            renderingService.buildErrorScene("Unprocessable message from server: " + e.getMessage());
        }

//                    try {
//
//                        Integer myNum = ThreadLocalRandom.current().nextInt(0, 10);
//                        sendMessage("name" + myNum.toString());
//                        System.out.println(receiveMessage());
//                        sendMessage("name" + myNum.toString());
//                        System.out.println(receiveMessage());
//                        System.out.println(receiveMessage());
//                        System.out.println(receiveMessage());
//                        System.out.println(receiveMessage());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
    }

    protected void startConnection(final String ip, final int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(String msg) throws ConnectException {
        if (out == null) {
            throw new ConnectException("There is no connection with server");
        }
        out.println(msg);
    }

    @Override
    public String receiveMessage() throws IOException {
        if (in == null) {
            throw new ConnectException("There is no connection with server");
        }
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        if (in == null || out == null || clientSocket == null) {
            throw new ConnectException("There is no connection with server");
        }
        in.close();
        out.close();
        clientSocket.close();
    }
}
