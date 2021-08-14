package server;

import client.Request;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseService {
    public static final String DATABASE_PATH = "./src/server/data/db.json";
    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    public DatabaseService() {
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public void removeFromDatabase() {
        try {
            writeLock.lock();
            Files.writeString(Paths.get(DATABASE_PATH), "");
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }
    }

    public String readFromDatabase(String key) {
        File databaseFile = new File(DATABASE_PATH);
        String result = null;
        try(Scanner scanner = new Scanner(databaseFile)) {
            readLock.lock();
            while (scanner.hasNextLine()) {
                String str = scanner.nextLine();
                if (str.contains(key)) {
                    result = str;
                    return result;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No file found: " + DATABASE_PATH);
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public void writeFileAsString(String key, String value, Gson gson) {
        try {
            writeLock.lock();
            Request request = new Request();
            request.setKey(key);
            request.setValue(value);
            Files.writeString(Paths.get(DATABASE_PATH), gson.toJson(request));
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            writeLock.unlock();
        }
    }
}
