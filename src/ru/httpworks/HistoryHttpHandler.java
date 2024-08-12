package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import ru.taskmanagment.TaskManager;
import ru.taskmanagment.InMemoryHistoryManager;

import java.io.IOException;

public class HistoryHttpHandler extends BaseHttpHandler {
    TaskManager manager;

    public HistoryHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET": {
                    InMemoryHistoryManager inMemoryHistoryManager = manager.getInMemoryHistoryManager();
                    String response = HttpTaskServer.getGson().toJson(inMemoryHistoryManager.getHistory());
                    sendText(exchange, response);
                    break;
                }
                default: {
                    String response = HttpTaskServer.getGson().toJson("Command " + exchange.getRequestMethod()
                            + " is not allowed for branch '/history'");
                    sendBadCommand(exchange, response);
                }
            }
        } catch (IOException e) {
            String response = HttpTaskServer.getGson()
                    .toJson("Произошла ошибка Internal Server Error при обработке запроса ");
            sendInternalError(exchange, response);
        }
    }
}
