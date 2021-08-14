package server;

import client.Request;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 34522;
    public static final String SET = "set";
    public static final String GET = "get";
    public static final String DELETE = "delete";
    public static final String EXIT = "exit";
    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String SERVER_STARTED = "Server started!";
    public static final String NO_SUCH_KEY = "No such key";

    public static void main(String[] args) {
        System.out.println(SERVER_STARTED);
        DatabaseService databaseService = new DatabaseService();
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        executor.submit(() -> { try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept();
                     DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                    String clientInput = input.readUTF();

                    Gson gson = new Gson();
                    Request request = gson.fromJson(clientInput, Request.class);

                    Response response = new Response();
                    String serverResponse;
                    String databaseResponse;

                    switch (request.getType()) {
                        case SET:
                            if (request.getValue() != null) {
                                databaseService.writeFileAsString(request.getKey(), request.getValue(), gson);
                                response.setResponse(OK);
                                serverResponse = gson.toJson(response);
                                output.writeUTF(serverResponse);
                                break;
                            }
                        case GET:
                            databaseResponse = databaseService.readFromDatabase(request.getKey());
                            if (databaseResponse != null) {
                                Response resp = gson.fromJson(databaseResponse, Response.class);
                                response.setResponse(OK);
                                response.setValue(resp.getValue());
                            } else {
                                response.setResponse(ERROR);
                                response.setReason(NO_SUCH_KEY);
                            }
                            serverResponse = gson.toJson(response);
                            output.writeUTF(serverResponse);
                            break;
                        case DELETE:
                            databaseResponse = databaseService.readFromDatabase(request.getKey());
                            if (databaseResponse != null) {
                                databaseService.removeFromDatabase();
                                response.setResponse(OK);
                            } else {
                                response.setResponse(ERROR);
                                response.setReason(NO_SUCH_KEY);
                            }

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
        }});
    }
}

