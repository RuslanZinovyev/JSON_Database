package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;
    public static final String SENT = "Sent: ";
    public static final String RECEIVED = "Received: ";
    public static final String CLIENT_STARTED = "Client started!";
    public static final String PATH_TO_CLIENT_FILE = "./src/client/data/";

    public static void main(String[] args) {
        ClientArgs clientArgs = new ClientArgs();
        JCommander helloCmd = JCommander.newBuilder()
                .addObject(clientArgs)
                .build();
        helloCmd.parse(args);

        String type = clientArgs.getType();
        String key = clientArgs.getKey();
        String value = clientArgs.getValue();
        String file = clientArgs.getFile();

        try(Socket socket = new Socket(ADDRESS, PORT);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        ) {
            System.out.println(CLIENT_STARTED);
            Request request = new Request();
            Gson gson = new Gson();
            String requestJson;
            if (type != null && key != null) {
                request.setType(type);
                request.setKey(key);
            }
            if (value != null) {
                request.setValue(value);

            }
            if (file != null) {
                String path = PATH_TO_CLIENT_FILE + file;
                File fileRequest = new File(path);
                try (Scanner scanner = new Scanner(fileRequest)) {
                    request = gson.fromJson(scanner.nextLine(), Request.class);
                } catch (FileNotFoundException e) {
                    System.out.println("No file found: " + path);
                }
            }
            requestJson = gson.toJson(request);
            output.writeUTF(requestJson);
            System.out.println(SENT + requestJson);
            String resp = input.readUTF();
            System.out.println(RECEIVED + resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
