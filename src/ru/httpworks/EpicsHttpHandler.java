package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import ru.taskmanagment.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class EpicsHttpHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHttpHandler(TaskManager manager) {
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
                        case null: //Для получения всего списка подходит параметр 'all' или отсутствие параметра
                        case 0: {
                            List<Epic> epics = manager.getEpicsList();
                            response = HttpTaskServer.getGson().toJson(epics);
                            sendText(exchange, response, 200);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed Epics ID is not a number. Getting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        default: {
                            Epic epic = manager.getEpicByCode(id);
                            if (epic == null) {
                                response = HttpTaskServer.getGson().toJson("Epic id= " + id + " not exists");
                                sendText(exchange, response, 404);
                            } else {
                                response = HttpTaskServer.getGson().toJson(epic);
                                sendText(exchange, response, 200);
                            }
                        }
                    }
                    break;
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
                                    .toJson("Appointed Epic ID is not a number. Changing is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case null: {
                            //Добавление нового эпика
                            String check = parametersChecking(param);
                            //если вернулась непустая строка, в параметрах обнаружилась ошибка
                            if (!check.isEmpty()) {
                                response = HttpTaskServer.getGson().toJson(check);
                                sendText(exchange, response, 405);
                            } else {
                                Epic epic = manager.createEpic(param.get("name"), param.get("description"), 0);
                                if (epic == null) {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Произошла ошибка Internal Server Error при обработке запроса");
                                    sendText(exchange, response, 500);
                                } else {
                                    response = HttpTaskServer.getGson()
                                            .toJson("Epic # " + epic.getCode() + " was created");
                                    sendText(exchange, response, 201);
                                }
                            }
                            break;
                        }
                        default: {
                            Epic epic = manager.getEpicByCode(id);
                            if (epic == null) {
                                response = HttpTaskServer.getGson()
                                        .toJson("Epic #" + id + " not exist. Changing is impossible");
                                sendText(exchange, response, 404);
                            } else {
                                epic.setName(param.get("name"));
                                epic.setDescription(param.get("description"));
                                manager.changeEpic(epic);
                                response = HttpTaskServer.getGson().toJson("Epic #" + id + " was changed");
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
                            response = HttpTaskServer.getGson()
                                    .toJson("Epics ID is absent. Deleting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case -1: {
                            response = HttpTaskServer.getGson()
                                    .toJson("Appointed Epics ID is not a number. Deleting is impossible");
                            sendText(exchange, response, 404);
                            break;
                        }
                        case 0: {
                            manager.deleteAllEpics();
                            response = HttpTaskServer.getGson()
                                    .toJson("All Epics and their Subtasks were deleted.");
                            sendText(exchange, response, 200);
                            break;
                        }
                        default: {
                            boolean result = manager.deleteEpic(id);
                            if (result) {
                                response = HttpTaskServer.getGson().toJson("Epic #" + id + " was deleted.");
                                sendText(exchange, response, 200);
                            } else {
                                response = HttpTaskServer.getGson().toJson("Epic #" + id + " not found.");
                                sendText(exchange, response, 404);
                            }
                        }
                    }
                    break;
                }
                default: {
                    response = HttpTaskServer.getGson().toJson("Command " + exchange.getRequestMethod()
                            + " is not allowed for branch '/epics'");
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
        return result;
    }
}
