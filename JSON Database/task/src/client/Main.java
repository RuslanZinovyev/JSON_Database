package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import static client.ClientArgs.*;

public class Main {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;

    public static void main(String[] args) {
        ClientArgs clientArgs = new ClientArgs();
        Gson gson = new GsonBuilder().create();

        JCommander.newBuilder()
                .addObject(clientArgs)
                .build()
                .parse(args);

        String type = clientArgs.getType();
        String key = clientArgs.getKey();
        String value = clientArgs.getValue();
        String file = clientArgs.getFile();

        try (
                Socket socket = new Socket(ADDRESS, PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            System.out.println(CLIENT_STARTED);

            Request request = new Request();
            String requestJson;

            if (type != null && key != null) {
                request.setType(type);
                request.setKey(JsonParser.parseString(gson.toJson(key)));
            }
            if (value != null) {
                request.setValue(JsonParser.parseString(gson.toJson(value)));

            }
            if (file != null) {
                String path = PATH_TO_CLIENT_FILE + file;
                String inputRequest = new String(Files.readAllBytes(Paths.get(path)));
                request = gson.fromJson(inputRequest, Request.class);
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
