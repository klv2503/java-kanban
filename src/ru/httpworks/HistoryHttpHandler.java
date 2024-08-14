package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import ru.taskmanagment.TaskManager;
import ru.taskmanagment.InMemoryHistoryManager;

import java.io.IOException;

public class HistoryHttpHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestMethod().equals("GET")) {
                InMemoryHistoryManager inMemoryHistoryManager = manager.getInMemoryHistoryManager();
                String response = HttpTaskServer.getGson().toJson(inMemoryHistoryManager.getHistory());
                sendText(exchange, response, 200);
            } else {
                String response = HttpTaskServer.getGson().toJson("Command " + exchange.getRequestMethod()
                        + " is not allowed for branch '/history'");
                sendText(exchange, response, 405);
            }
        } catch (IOException e) {
            String response = HttpTaskServer.getGson()
                    .toJson("Произошла ошибка Internal Server Error при обработке запроса ");
            sendText(exchange, response, 500);
        }
    }
}
