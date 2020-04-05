package unn.game.bugs.models;

public class Constants {
    public static final Integer SERVER_PORT = 8080;

    public static final Integer PLAYERS_COUNT = 2;
    public static final Integer FIELD_SIZE_X = 30;
    public static final Integer FIELD_SIZE_Y = 30;
    public static final Integer MOVES = 5;
    public static final Integer SHIFT = 3;

    public static final String CLIENT_CONNECTION_ERROR = "Client connection error";
    public static final String SERVER_CONNECTION_ERROR = "Server connection error";
    public static final String UNPROCESSABLE_MESSAGE_FROM_CLIENT = "Unprocessable message from client";
    public static final String UNPROCESSABLE_MESSAGE_TO_CLIENT = "Can't serialize message to send to client";
    public static final String UNPROCESSABLE_MESSAGE_FROM_SERVER = "Unprocessable message from server";
    public static final String UNPROCESSABLE_MESSAGE_TO_SERVER = "Can't serialize message to send to server";
}
