package ru.httpworks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.taskmanagment.FileBackedTaskManager;
import ru.taskmanagment.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer httpServer;
    static File fileName = Paths.get(System.getProperty("user.home"), "managesaver.txt").toFile();
    private static final TaskManager manager = FileBackedTaskManager.loadFromFile(fileName);

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        this.httpServer.createContext("/tasks", new TasksHttpHandler(manager));
        this.httpServer.createContext("/subtasks", new SubTasksHttpHandler(manager));
        this.httpServer.createContext("/epics", new EpicsHttpHandler(manager));
        this.httpServer.createContext("/history", new HistoryHttpHandler(manager));
        this.httpServer.createContext("/prioritized", new PriorityHttpHandler(manager));
    }

    public void start() {
        httpServer.start();
        System.out.println("Server started at port " + PORT);
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Server stopped");
    }

    public void stopServer() {
        Scanner sc = new Scanner(System.in);
        String st = "For closing server input 'stop'";
        System.out.println(st);
        while (!st.equals("stop")) {
            st = sc.nextLine();
        }
        stop();
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(manager);
        httpTaskServer.start();
        httpTaskServer.stopServer();
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());
        return gsonBuilder.create();
    }

}
