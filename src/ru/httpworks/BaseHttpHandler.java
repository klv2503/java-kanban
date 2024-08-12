package ru.httpworks;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class BaseHttpHandler implements HttpHandler {

    protected void sendText(HttpExchange h, String text, int responseCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(responseCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected Integer getTaskId(String path) {
        String[] pathToElements = path.split("/");
        if (pathToElements.length < 3) {
            return null;
        }
        if (pathToElements[2].equalsIgnoreCase("all")) return 0; //для обработки ситуации "all"
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
            for (String parameter : parameters) {
                int ind = parameter.indexOf("=");
                String str = parameter.substring(ind + 1)
                        .replace("%3A", ":").replace("%20", " ");
                resultMap.put(parameter.substring(0, ind).toLowerCase(), str);
            }
            return resultMap;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
