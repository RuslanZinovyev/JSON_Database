package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;
    public static final String SENT = "Sent: ";
    public static final String RECEIVED = "Received: ";
    public static final String CLIENT_STARTED = "Client started!";


    public static void main(String[] args) {
        ClientArgs clientArgs = new ClientArgs();
        JCommander helloCmd = JCommander.newBuilder()
                .addObject(clientArgs)
                .build();
        helloCmd.parse(args);

        String type = clientArgs.getType();
        String key = clientArgs.getKey();
        String value = clientArgs.getValue();

        try(Socket socket = new Socket(ADDRESS, PORT);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            System.out.println(CLIENT_STARTED);
            Request request = new Request();
            Gson gson = new Gson();
            String requestJson;
            request.setType(type);
            request.setKey(key);
            if (value != null) {
                request.setValue(value);

            }
            requestJson = gson.toJson(request);
            output.writeUTF(requestJson);
            System.out.println(SENT + requestJson);
            System.out.println(RECEIVED + input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
