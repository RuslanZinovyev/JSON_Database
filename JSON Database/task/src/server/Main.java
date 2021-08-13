package server;

import client.Request;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

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
        HashMap<String, String> database = new HashMap<>();

        System.out.println(SERVER_STARTED);
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
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

                    switch (request.getType()) {
                        case SET:
                            if (request.getValue() != null) {
                                database.put(request.getKey(), request.getValue());
                                response.setResponse(OK);
                                serverResponse = gson.toJson(response);
                                output.writeUTF(serverResponse);
                                break;
                            }
                        case GET:
                            if (database.get(request.getKey()) == null) {
                                response.setResponse(ERROR);
                                response.setReason(NO_SUCH_KEY);
                            } else {
                                response.setResponse(OK);
                                response.setValue(database.get(request.getKey()));
                            }
                            serverResponse = gson.toJson(response);
                            output.writeUTF(serverResponse);
                            break;
                        case DELETE:
                            if (database.get(request.getKey()) == null) {
                                response.setResponse(ERROR);
                                response.setReason(NO_SUCH_KEY);
                            } else {
                                database.remove(request.getKey());
                                response.setResponse(OK);
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
            e.printStackTrace();
        }
    }

}

