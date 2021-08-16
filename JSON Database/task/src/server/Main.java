package server;

import client.Request;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static server.DatabaseService.*;

public class Main {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;

    public static void main(String[] args) {
        System.out.println(SERVER_STARTED);
        DatabaseService databaseService = new DatabaseService();
        databaseService.init();
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
                while (!serverSocket.isClosed()) {
                    try (Socket socket = serverSocket.accept();
                         DataInputStream input = new DataInputStream(socket.getInputStream());
                         DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                    ) {

                        Gson gson = new Gson();
                        Request request = gson.fromJson(input.readUTF(), Request.class);

                        Response response = new Response();
                        String serverResponse;
                        JsonElement databaseResponse;

                        switch (request.getType()) {
                            case SET:
                                databaseService.setValueByKey(request.getKey(), request.getValue());
                                response.setResponse(OK);
                                serverResponse = gson.toJson(response);
                                output.writeUTF(serverResponse);
                                break;
                            case GET:
                                databaseResponse = databaseService.getValueByKey(request.getKey());
                                response.setResponse(OK);
                                response.setValue(databaseResponse);
                                serverResponse = gson.toJson(response);
                                output.writeUTF(serverResponse);
                                break;
                            case DELETE:
                                databaseService.removeValueByKey(request.getKey());
                                response.setResponse(OK);
                                serverResponse = gson.toJson(response);
                                output.writeUTF(serverResponse);
                                break;
                            case EXIT:
                                serverSocket.close();
                                response.setResponse(OK);
                                serverResponse = gson.toJson(response);
                                output.writeUTF(serverResponse);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        });
    }
}

