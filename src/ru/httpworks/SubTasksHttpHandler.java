package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import ru.taskmanagment.*;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class SubTasksHttpHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubTasksHttpHandler(TaskManager manager) {
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
                            List<SubTask> subTasks = manager.getSubTasksList();
                            response = HttpTaskServer.getGson().toJson(subTasks);
                            sendText(exchange, response, 200);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed SubTasks ID is not a number. Getting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        default: {
                            SubTask subTask = manager.getSubTaskWithId(id);
                            if (subTask == null) {
                                response = HttpTaskServer.getGson().toJson("SubTask id= " + id + " not exists");
                                sendText(exchange, response, 404);
                            } else {
                                response = HttpTaskServer.getGson().toJson(subTask);
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
                            //Добавление новой subTask
                            String check = parametersChecking(param);
                            //если вернулась непустая строка, в параметрах обнаружилась ошибка
                            if (!check.isEmpty()) {
                                response = HttpTaskServer.getGson().toJson(check);
                                sendText(exchange, response, 405);
                            } else {
                                //проверяем валидность временных характеристик
                                LocalDateTime lockDT = LocalDateTime.parse(param.get("starttime"),
                                        TimeManager.dateTimeFormatter);
                                int duration = Integer.parseInt(param.get("duration"));
                                if (!manager.isValidTime(lockDT, Duration.ofMinutes(duration))) {
                                    response = HttpTaskServer.getGson().toJson("Time for SubTask is not free");
                                    sendText(exchange, response, 406);
                                    break;
                                }
                                //проверяем существование task
                                int taskId = Integer.parseInt(param.get("task"));
                                if (manager.getTaskWithId(taskId) == null) {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Task #" + taskId
                                                    + " not exist. Making of Subtask is impossible");
                                    sendText(exchange, response, 404);
                                    break;
                                }
                                //проверяем существование epic
                                int epicId = Integer.parseInt(param.get("epic"));
                                if (manager.getEpicByCode(epicId) == null) {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Epic #" + epicId
                                                    + " not exist. Making of Subtask is impossible");
                                    sendText(exchange, response, 404);
                                    break;
                                }
                                SubTask subTask = manager.createSubTaskForEpic(taskId, epicId, lockDT, duration);
                                if (subTask == null) {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Произошла ошибка Internal Server Error при обработке запроса");
                                    sendText(exchange, response, 500);
                                } else {
                                    response = HttpTaskServer.getGson()
                                            .toJson("SubTask added to Epic # " + Integer.parseInt(param.get("epic")));
                                    sendText(exchange, response, 201);
                                }
                            }
                            break;
                        }
                        default: {
                            SubTask subTask = manager.getSubTaskWithId(id);
                            //проверяем наличие subTask
                            if (subTask == null) {
                                response = HttpTaskServer.getGson()
                                        .toJson("SubTask #" + id + " not exist. Changing is impossible");
                                sendText(exchange, response, 404);
                                break;
                            }
                            int epicId = manager.getSubTasksEpic(subTask);
                            //проверяем, меняется ли время и продолжительность и корректно ли
                            if (param.containsKey("starttime") || param.containsKey("duration")) {
                                LocalDateTime lockDT = param.containsKey("starttime") ?
                                        LocalDateTime.parse(param.get("starttime"), TimeManager.dateTimeFormatter)
                                        : subTask.getStartTime();
                                long duration = param.containsKey("duration") ? Integer.parseInt(param.get("duration"))
                                        : subTask.getDurationInMinutes();
                                manager.removeSorted(subTask);
                                if (manager.isValidTime(lockDT, Duration.ofMinutes(duration))) {
                                    subTask.setStartTime(lockDT);
                                    subTask.setDuration(Duration.ofMinutes(duration));
                                } else {
                                    manager.addSorted(subTask);
                                    response = HttpTaskServer.getGson().toJson("Time for SubTask is not free");
                                    sendText(exchange, response, 406);
                                    break;
                                }
                            }
                            if (param.containsKey("status"))
                                subTask.setStatus(Status.valueOf(param.get("status")));
                            manager.deleteEpicsSubTask(epicId, subTask.getOwnCode());
                            manager.addNewSubToEpic(epicId, subTask);
                        }
                    }
                    response = HttpTaskServer.getGson().toJson("SubTask #" + id + " was changed");
                    sendText(exchange, response, 201);
                    break;
                }
                case "DELETE": {
                    Integer id = getTaskId(exchange.getRequestURI().getPath());
                    switch (id) {
                        case null: {
                            response = HttpTaskServer.getGson()
                                    .toJson("SubTasks ID is absent. Deleting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed SubTasks ID is not a number. Deleting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case 0: {
                            manager.deleteAllSubTasks();
                            response = HttpTaskServer.getGson().toJson("All Subtasks were deleted.");
                            sendText(exchange, response, 200);
                            break;
                        }
                        default: {
                            boolean result = manager.removeSubTaskWithId(id);
                            if (result) {
                                response = HttpTaskServer.getGson().toJson("SubTask #" + id + " was deleted.");
                                sendText(exchange, response, 200);
                            } else {
                                response = HttpTaskServer.getGson().toJson("SubTask #" + id + " not found.");
                                sendText(exchange, response, 404);
                            }
                        }
                    }
                    break;
                }
                default: {
                    response = HttpTaskServer.getGson().toJson("Command " + exchange.getRequestMethod()
                            + " is not allowed for branch '/subtasks'");
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
        if (!param.containsKey("task") || !param.containsKey("epic") || !param.containsKey("starttime")
                || !param.containsKey("duration"))
            return "Запрос на добавление не содержит всех необходимых параметров";
        try {
            int taskId = Integer.parseInt(param.get("task"));
            int epicId = Integer.parseInt(param.get("epic"));
            int duration = Integer.parseInt(param.get("duration"));
        } catch (NumberFormatException e) {
            return "Неверный формат числовых параметров";
        }
        try {
            LocalDateTime lockDT = LocalDateTime.parse(param.get("starttime"), TimeManager.dateTimeFormatter);
        } catch (DateTimeException e) {
            return "Неверный формат времени и/или даты старта";
        }
        return result;
    }
}
