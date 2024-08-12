package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import ru.taskmanagment.Task;
import ru.taskmanagment.TaskManager;

import java.io.IOException;
import java.util.List;

public class PriorityHttpHandler extends BaseHttpHandler {
    TaskManager manager;

    public PriorityHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            switch (exchange.getRequestMethod()) {
                case "GET": {
                    List<Task> tasks = manager.getPrioritizedTasks();
                    response = HttpTaskServer.getGson().toJson(tasks);
                    sendText(exchange, response);
                    break;
                }
                default: {
                    response = HttpTaskServer.getGson().toJson("Command " + exchange.getRequestMethod()
                            + " is not allowed for branch '/prioritized'");
                    sendBadCommand(exchange, response);
                }
            }
        } catch (IOException e) {
            response = HttpTaskServer.getGson()
                    .toJson("Произошла ошибка Internal Server Error при обработке запроса ");
            sendInternalError(exchange, response);
        }
    }
}
