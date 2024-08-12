package ru.taskmanagment;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import ru.httpworks.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу name = TestHttpClient, description = Task for testing
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/?name=TestHttpClient&description=Task%20for%20testing");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasksList();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("TestHttpClient", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddInvalidTask() throws IOException, InterruptedException {
        // для создания задач следует указать ее name и description. Пробуем создать без одного из этих полей
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/?description=Task%20for%20testing");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
        // проверяем, что задача не создана
        List<Task> tasksFromManager = manager.getTasksList();
        assertTrue(tasksFromManager.isEmpty(), "список задач не пуст");
        url = URI.create("http://localhost:8080/tasks/?name=TestHttpClient");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
        // проверяем, что задача снова не создана
        tasksFromManager = manager.getTasksList();
        assertTrue(tasksFromManager.isEmpty(), "список задач не пуст");
    }

    @Test
    public void testChangeTimeAndDuration() throws IOException, InterruptedException {
        // Создаем задачу, а затем устанавливаем у нее startTime и duration
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/?name=TestHttpClient&description=Task%20for%20testing");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/1?startTime=08%3A15%2004-09-2024&duration=15");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        Task task = manager.getTaskWithId(1);
        assertNotNull(task, "вместо созданной задачи получен null");
        String str = task.getStartTime().format(TimeManager.dateTimeFormatter);
        assertEquals("08:15 04-09-2024", str, "startTime задачи неверен");
        long zeit = task.getDurationInMinutes();
        assertEquals(15, zeit, "duration не равно 15");
    }

    @Test
    public void testCreateAndDeleteTask() throws IOException, InterruptedException {
        // Создаем задачу, а затем устанавливаем у нее startTime и duration
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/?name=TestHttpClient&description=Task%20for%20testing");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());
        Task task = manager.getTaskWithId(1);
        assertNull(task, "поскольку задача удалена, вместо должен быть получен null");
        //список задач должен быть пуст
        List<Task> tasksFromManager = manager.getTasksList();
        assertTrue(tasksFromManager.isEmpty(), "список задач не пуст");
    }

    @Test
    public void testGetNonExisting() throws IOException, InterruptedException {
        // Пробуем получить объекты без их генерации
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/subtasks/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        //Создаем эпик и прверяем создание
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/?name=Http%20testing&description=Creation%20of%20epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась один эпик с корректным именем
        List<Epic> epicsFromManager = manager.getEpicsList();

        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals("Http testing", epicsFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddInvalidEpic() throws IOException, InterruptedException {
        // для создания эпика следует указать его name и description. Пробуем создать без одного из этих полей
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/?description=Epic%20for%20testing");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
        // проверяем, что эпик не создан
        List<Epic> epicsFromManager = manager.getEpicsList();
        assertTrue(epicsFromManager.isEmpty(), "список эпиков не пуст");
        url = URI.create("http://localhost:8080/epics/?name=TestHttpClient");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
        // проверяем, что задача снова не создана
        epicsFromManager = manager.getEpicsList();
        assertTrue(epicsFromManager.isEmpty(), "список задач не пуст");
    }

    @Test
    public void testCreateAndDeleteEpic() throws IOException, InterruptedException {
        // Создаем задачу, а затем устанавливаем у нее startTime и duration
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/?name=Http%20testing&description=Creation%20of%20epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());
        Epic epic = manager.getEpicByCode(1);
        assertNull(epic, "поскольку эпик удален, вместо него должен быть получен null");
        //список задач должен быть пуст
        List<Epic> epicsFromManager = manager.getEpicsList();
        assertTrue(epicsFromManager.isEmpty(), "список задач не пуст");
    }

    @Test
    public void testCreateTaskEpicSubtask() throws IOException, InterruptedException {
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/?name=Http%20testing&description=Creation%20of%20epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/?name=TestHttpClient&description=Task%20for%20testing");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/subtasks"
                + "?task=1&epic=1&startTime=10%3A30%2004-09-2024&duration=15&status=NEW");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
    }

    @Test
    public void testHistory() throws IOException, InterruptedException {
        // Ветка /history отрабатывает только запрос GET на остальные выдает ответ "недопустимая команда"
        // То, что список пуст не имеет значения, запрос все равно должен отрабатываться
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());
        url = URI.create("http://localhost:8080/history");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
        url = URI.create("http://localhost:8080/history");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
    }

    @Test
    public void testPrioritized() throws IOException, InterruptedException {
        // Ветка /prioritized отрабатывает только запрос GET на остальные выдает ответ "недопустимая команда"
        // То, что список пуст не имеет значения, запрос все равно должен отрабатываться
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());
        url = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
        url = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(405, response.statusCode());
    }

    @Test
    public void testInteractions() throws IOException, InterruptedException {
        // Создаем задачу, эпик(без него нельзя создать подзадачу) и подзадачу, которая генерируется
        // сразу со startTime и duration. Затем пробуем установить ыремя у task так чтобы она пересеклась
        // с subTask, а после без пересечения
        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/?name=Http%20testing&description=Creation%20of%20epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/?name=TestHttpClient&description=Task%20for%20testing");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/subtasks"
                + "?task=1&epic=1&startTime=10%3A30%2004-09-2024&duration=15&status=NEW");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
        url = URI.create("http://localhost:8080/tasks/1?startTime=10%3A40%2004-09-2024&duration=15");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(406, response.statusCode());
        //Назначаем время выполнения task так, чтобы она следовала за subTask
        url = URI.create("http://localhost:8080/tasks/1?startTime=10%3A45%2004-09-2024&duration=15");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());
    }

}
