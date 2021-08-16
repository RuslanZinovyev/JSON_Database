package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import server.exception.NoSuchKeyException;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseService {
    public static final String DATABASE_PATH = "./src/server/data/db.json";
    public static final String SET = "set";
    public static final String GET = "get";
    public static final String DELETE = "delete";
    public static final String EXIT = "exit";
    public static final String OK = "OK";
    public static final String SERVER_STARTED = "Server started!";

    private final Lock readLock;
    private final Lock writeLock;
    private JsonObject database;

    public DatabaseService() {
        ReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public void init() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(DATABASE_PATH)));
            database = new Gson().fromJson(content, JsonObject.class);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public JsonElement getValueByKey(JsonElement key) {
        try {
            readLock.lock();
            if (key.isJsonPrimitive() && database.has(key.getAsString())) {
                return database.get(key.getAsString());
            } else if (key.isJsonArray()) {
                return findElement(key.getAsJsonArray(), false);
            }
            throw new NoSuchKeyException();
        } finally {
            readLock.unlock();
        }
    }

    public void setValueByKey(JsonElement key, JsonElement value) {
        try {
            writeLock.lock();
            if (database == null) {
                database = new JsonObject();
                database.add(key.getAsString(), value);
            } else {
                if (key.isJsonPrimitive()) {
                    database.add(key.getAsString(), value);
                } else if (key.isJsonArray()) {
                    JsonArray keys = key.getAsJsonArray();
                    String addValue = keys.remove(keys.size() - 1).getAsString();
                    findElement(keys, true).getAsJsonObject().add(addValue, value);
                } else {
                    throw new NoSuchKeyException();
                }
            }
            writeToDatabase();
        } finally {
            writeLock.unlock();
        }
    }

    public void removeValueByKey(JsonElement key) {
        try {
            writeLock.lock();
            if (key.isJsonPrimitive() && database.has(key.getAsString())){
                database.remove(key.getAsString());
            } else if (key.isJsonArray()){
                JsonArray keys = key.getAsJsonArray();
                String toDelete = keys.remove(keys.size() - 1).getAsString();
                findElement(keys, true).getAsJsonObject().remove(toDelete);
            }
            writeToDatabase();
        } finally {
            writeLock.unlock();
        }
    }

    private JsonElement findElement(JsonArray keys, boolean isCreated) {
        JsonElement temp = database;
        if (isCreated) {
            for (JsonElement key : keys) {
                if (!temp.getAsJsonObject().has(key.getAsString())) {
                    temp.getAsJsonObject().add(key.getAsString(), new JsonObject());
                }
                temp = temp.getAsJsonObject().get(key.getAsString());
            }
        } else {
            for (JsonElement key : keys) {
                // if key is not primitive or database doesn't have this key
                if (!key.isJsonPrimitive() || !temp.getAsJsonObject().has(key.getAsString())) {
                    throw new NoSuchKeyException();
                }
                temp = temp.getAsJsonObject().get(key.getAsString());
            }
        }
        return temp;
    }

    private void writeToDatabase() {
        try {
            FileWriter writer = new FileWriter(DATABASE_PATH);
            writer.write(database.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
