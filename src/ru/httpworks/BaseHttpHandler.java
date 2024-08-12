package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class BaseHttpHandler implements HttpHandler {

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreatedOrUpdated(HttpExchange h, String text) throws IOException {
        //для отправки ответа в случае, если успешно создан или изменен
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        //для отправки ответа в случае, если объект не был найден
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendBadCommand(HttpExchange h, String text) throws IOException {
        //Для отправки ответа в случае, если метод запроса не обрабатывается в данном разделе.
        //Например, для /history и /prioritized не работают никакие методы кроме GET, но пользователю нужно
        //об этом сообщить. Код 405 подобрал из списка как более-менее подходящий
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(405, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        //для отправки ответа, если при создании или обновлении задача пересекается с уже существующими.
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendInternalError(HttpExchange h, String text) throws IOException {
        //для отправки сообщения о внутренней ошибке сервера
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(500, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected Integer getTaskId(String path) {
        String[] pathToElements = path.split("/");
        if (pathToElements.length < 3) {
            return null;
        }
        if (pathToElements[2].toLowerCase().equals("all")) return 0; //для обработки ситуации "all"
        try {
            return Integer.parseInt(pathToElements[2]);
        } catch (NumberFormatException e) {
            return -1; //ошибка ввода. принятый параметр не является числом или опцией "all"
        }
    }

    protected HashMap<String, String> getParameter(String requestParameters) {
        HashMap<String, String> resultMap = new HashMap<>();
        try {
            if (requestParameters.isEmpty()) return resultMap;
            String[] parameters = requestParameters.split("&");
            for (int i = 0; i < parameters.length; i++) {
                int ind = parameters[i].indexOf("=");
                String str = parameters[i].substring(ind + 1)
                        .replace("%3A", ":").replace("%20", " ");
                resultMap.put(parameters[i].substring(0, ind).toLowerCase(), str);
            }
            return resultMap;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
