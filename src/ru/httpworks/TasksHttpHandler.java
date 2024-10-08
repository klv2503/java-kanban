package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import ru.taskmanagment.Task;
import ru.taskmanagment.TaskManager;
import ru.taskmanagment.TimeManager;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class TasksHttpHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            switch (exchange.getRequestMethod()) {
                case "GET": {
                    Integer id = getTaskId(exchange.getRequestURI().getPath());
                    switch (id) {
                        case null:
                        case 0: {
                            List<Task> tasks = manager.getTasksList();
                            response = HttpTaskServer.getGson().toJson(tasks);
                            sendText(exchange, response, 200);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed Tasks ID is not a number. Getting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        default: {
                            Task task = manager.getTaskWithId(id);
                            if (task == null) {
                                response = HttpTaskServer.getGson().toJson("Task id= " + id + " not exists");
                                sendText(exchange, response, 404);
                            } else {
                                response = HttpTaskServer.getGson().toJson(task);
                                sendText(exchange, response, 200);
                            }
                        }
                        break;
                    }
                }
                case "POST": {
                    String request = exchange.getRequestURI().toString();
                    HashMap<String, String> param =
                            getParameter(request.substring(request.indexOf("?") + 1));
                    if (param == null) {
                        response = HttpTaskServer.getGson().toJson("Error in parameters found");
                        sendText(exchange, response, 405);
                        break;
                    }
                    Integer id = getTaskId(exchange.getRequestURI().getPath());
                    switch (id) {
                        case 0: {
                            response = HttpTaskServer.getGson().toJson("Request POST not allows option 'all'");
                            sendText(exchange, response, 405);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed SubTasks ID is not a number. Changing is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case null: {
                            //Добавление новой task
                            String check = parametersChecking(param);
                            //если вернулась непустая строка, в параметрах обнаружилась ошибка
                            if (!check.isEmpty()) {
                                response = HttpTaskServer.getGson().toJson(check);
                                sendText(exchange, response, 405);
                            } else {
                                Task task = manager.createTask(param.get("name"), param.get("description"));
                                if (task == null) {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Произошла ошибка Internal Server Error при обработке запроса");
                                    sendText(exchange, response, 500);
                                } else {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Task # " + task.getCode() + " was created");
                                    sendText(exchange, response, 201);
                                }
                            }
                            break;
                        }
                        default: {
                            Task task = manager.getTaskWithId(id);
                            if (task == null) {
                                response = HttpTaskServer.getGson()
                                        .toJson("Task #" + id + " not exist. Changing is impossible");
                                sendText(exchange, response, 404);
                                break;
                            }
                            if (param.containsKey("name"))
                                task.setName(param.get("name"));
                            if (param.containsKey("description"))
                                task.setDescription(param.get("description"));
                            if (param.containsKey("starttime") || param.containsKey("duration")) {
                                LocalDateTime lockDT = param.containsKey("starttime") ?
                                        LocalDateTime.parse(param.get("starttime"), TimeManager.dateTimeFormatter)
                                        : task.getStartTime();
                                long duration = param.containsKey("duration") ? Integer.parseInt(param.get("duration"))
                                        : task.getDurationInMinutes();
                                if (task.getStartTime() != null)
                                    manager.removeSorted(task);
                                if (manager.makeTaskExecutable(task, lockDT, duration)) {
                                    manager.renewTask(task);
                                    response = HttpTaskServer.getGson().toJson("Task #" + id + " was changed");
                                    sendText(exchange, response, 201);
                                } else {
                                    if (task.getStartTime() != null)
                                        manager.addSorted(task);
                                    response = HttpTaskServer.getGson().toJson("Time for task is not free");
                                    sendText(exchange, response, 406);
                                }
                            } else {
                                manager.renewTask(task);
                                response = HttpTaskServer.getGson().toJson("Task #" + id + " was changed");
                                sendText(exchange, response, 201);
                            }
                        }
                    }
                    break;
                }
                case "DELETE": {
                    Integer id = getTaskId(exchange.getRequestURI().getPath());
                    switch (id) {
                        case null: {
                            response = HttpTaskServer.getGson().toJson("Tasks ID is absent. Deleting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed Tasks ID is not a number. Deleting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case 0: {
                            manager.deleteAllTasks();
                            response = HttpTaskServer.getGson().toJson("All tasks were deleted.");
                            sendText(exchange, response, 200);
                            break;
                        }
                        default: {
                            boolean result = manager.removeTaskWithId(id);
                            if (result) {
                                response = HttpTaskServer.getGson().toJson("Tasks #" + id + " was deleted.");
                                sendText(exchange, response, 200);
                            } else {
                                response = HttpTaskServer.getGson().toJson("Tasks #" + id + " not found.");
                                sendText(exchange, response, 404);
                            }
                        }
                    }
                    break;
                }
                default: {
                    response = HttpTaskServer.getGson().toJson("Command " + exchange.getRequestMethod()
                            + " is not allowed for branch '/tasks'");
                    sendText(exchange, response, 405);
                }
            }
        } catch (IOException e) {
            response = HttpTaskServer.getGson()
                    .toJson("Произошла ошибка Internal Server Error при обработке запроса ");
            sendText(exchange, response, 500);
        }
    }

    public String parametersChecking(HashMap<String, String> param) {
        String result = "";
        if (!param.containsKey("name") || !param.containsKey("description"))
            return "Запрос на добавление не содержит всех необходимых параметров";
        if (param.containsKey("starttime"))
            try {
                LocalDateTime lockDT = LocalDateTime.parse(param.get("starttime"), TimeManager.dateTimeFormatter);
            } catch (DateTimeException e) {
                return "Неверный формат времени и/или даты старта";
            }
        if (param.containsKey("duration"))
            try {
                int duration = Integer.parseInt(param.get("duration"));
            } catch (NumberFormatException e) {
                return "Неверный формат числовых параметров";
            }
        return result;
    }

}
