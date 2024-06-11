package ru.taskmanagment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    static InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    static EpicAdmin epicAdmin = new EpicAdmin();
    static int numberOfGeneratedTasks = 10;
    static int numberOfGeneratedEpics = 5;
    static Random rnd = new Random();

    @BeforeAll
    public static void initialGeneration() {
        for (int i = 0; i < numberOfGeneratedTasks; i++) {
            inMemoryTaskManager.makeTask("Shortly", "Long description");
        }
        for (int i = 0; i < numberOfGeneratedEpics; i++) {
            String name = "Epic #";
            String description = "Description of Epic #";
            int taskNumber = rnd.nextInt(numberOfGeneratedTasks - 1) + 1;
            inMemoryTaskManager.makeTestEpic(name, description, taskNumber);
        }
    }

    //То, что я смог придумать для тестирования Managers
    @Test
    public void shouldCreateTaskManagerAndSomethingDone() {
        TaskManager taskManager = Managers.getDefault();
        boolean tstBool = taskManager.isEpicExist(2);
        assertEquals(true, tstBool, "taskManager не нашёл эпика № 2");
    }

    //Тестирование InMemoryTaskManager

    @Test
    public void shouldCreateTaskAndFindAfter() {
        String name = "Task creation";
        String description = "New task for testing of creation and seeking";
        String errorMessage = "Данные найденной задачи не совпадают с данными только что созданной";
        inMemoryTaskManager.createTask(name, description);
        int index = inMemoryTaskManager.getTaskCounter();
        Task task = inMemoryTaskManager.getTaskWithId(index);
        boolean resoult = task.name.equals(name) && task.description.equals(description);
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void shouldCreateEpicAndFindAfter() {
        String name = "Epic creation";
        String description = "New epic for testing of creation and seeking";
        String errorMessage = "Данные найденного эпика не совпадают с данными только что созданного";
        inMemoryTaskManager.createEpic(name, description, 0);
        int index = inMemoryTaskManager.getEpicCounter();
        Epic epic = inMemoryTaskManager.getEpicByCode(index);
        boolean resoult = epic.name.equals(name) && epic.description.equals(description);
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void shouldCreateSubTaskAndFindAfter() {
        int epicId = 2;
        int taskId = 6;
        int place = 3;
        String errorMessage = "Данные найденной подзадачи не совпадают с данными задачи-прототипа";
        Task task = inMemoryTaskManager.getTaskWithId(6);
        inMemoryTaskManager.addNewSubToEpic(epicId, taskId, place);
        if (place > inMemoryTaskManager.getHowSubTasks(epicId)) {
            place = inMemoryTaskManager.getHowSubTasks(epicId);
        }
        SubTask subTask = inMemoryTaskManager.getEpicsSubTaskByNumber(epicId, place);
        boolean resoult = subTask.name.equals(task.name) && subTask.description.equals(task.description);
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void shouldFindExistingTask8() {
        boolean tstBool = inMemoryTaskManager.isTaskExist(8);
        assertEquals(true, tstBool, "Не нашли задачу № 8, которая должна быть в списке");
    }

    @Test
    public void shouldNotFindTask50() {
        boolean tstBool = inMemoryTaskManager.isTaskExist(50);
        assertFalse(tstBool, "Задача найдена, хотя ее не должно быть.");
    }

    @Test
    public void shouldTestThatMethodReturnsNullThenTaskNotExist() {
        int id = 25;
        Task task = inMemoryTaskManager.getTaskWithId(id);
        assertNull(task, "Task № " + id + " is not equal to null.");
    }

    @Test
    public void shouldCompareTask1AndTask2() {
        int bound = inMemoryTaskManager.getTaskCounter();
        int id1 = rnd.nextInt(bound);
        Task task1 = inMemoryTaskManager.getTaskWithId(id1);
        while (task1 == null) {
            id1 = rnd.nextInt(bound);
            task1 = inMemoryTaskManager.getTaskWithId(id1);
        }
        int id2 = rnd.nextInt(bound);
        Task task2 = inMemoryTaskManager.getTaskWithId(id2);
        while (task2 == null) {
            id2 = rnd.nextInt(bound);
            task2 = inMemoryTaskManager.getTaskWithId(id2);
        }
        if (id1 == id2) {
            assertEquals(task1, task2, "id1 = id2, but task1 is not equal task2");
        } else {
            assertNotEquals(task1, task2, "id1 is not equal id2, but task1 is equal task2");
        }
    }

/*    @Test
    public void shouldSayEpicCantBeOwnSubtask() {
        String errorMessage = "Эпик добавлен самому себе в качестве подзадачи";
        boolean resoult = inMemoryTaskManager.addEpicAsSubTask(1, 1);
        assertEquals(false, resoult, errorMessage);
    } */

    // Блок тестирования HistoryManager
    @Test
    public void testingMaximalSizeOfHistoryManagersArray() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = inMemoryTaskManager.getTaskWithId(i + 1);
            epic = inMemoryTaskManager.getEpicByCode(i + 1);
            subTask = inMemoryTaskManager.getEpicsSubTaskByNumber(i + 1, 1);
        }
        int arraySize = inMemoryTaskManager.inMemoryHistoryManager.getHistory().size();
        assertEquals(10, arraySize, "Size of HistoryManagers Array is not equal to 10.");
    }

    @Test
    public void testingHistoryManagersArrayKeepsDifferentTypes() {
        // Проверка на Task оказалась бессмысленной,
        // т.к. проверка, является ли объект истории типа Task всегда даёт true
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = inMemoryTaskManager.getTaskWithId(i + 1);
            epic = inMemoryTaskManager.getEpicByCode(i + 1);
            subTask = inMemoryTaskManager.getEpicsSubTaskByNumber(i + 1, 1);
        }
        ArrayList<Task> currentArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
        String errorMessage = "HistoryManager keep not SubTask";
        boolean result = currentArray.get(0) instanceof SubTask;
        assertEquals(true, result, errorMessage);
        errorMessage = "HistoryManager keep not Epic";
        result = currentArray.get(2) instanceof Epic;
        assertEquals(true, result, errorMessage);
    }

    @Test
    public void testingHistoryManagersArrayKeepsDifferentVersions() {
        Task task = inMemoryTaskManager.getTaskWithId(1);
        int code = task.code;
        String oldName = task.name;
        String oldDescription = task.description;

        // code не меняем, т.к. тогда это будет уже другая задача, а не новая версия той же
        String newName = "New tasks name";
        String newDescription = "New tasks description";
        Task newTask = new Task(code, newName, newDescription);
        inMemoryTaskManager.renewTask(newTask);
        task = inMemoryTaskManager.getTaskWithId(1);

        ArrayList<Task> currentArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
        if (!currentArray.isEmpty()) {
            int arraySize = currentArray.size() - 1;
            task = currentArray.get(arraySize - 1);
            assertEquals(oldName, task.name, "Имя старой версии задачи отличается от первоначального.");
            assertEquals(oldDescription, task.description,
                    "Описание старой версии задачи отличается от первоначального.");
            task = currentArray.get(arraySize);
            assertEquals(newName, task.name, "Имя новой версии задачи отличается от заново заданного.");
            assertEquals(newDescription, task.description,
                    "Описание новой версии задачи отличается от нового описания.");
        }
    }

    //Блок тестирования epicAdmin
    @Test
    public void shouldHoldObjectsSubTaskAndEpic() {
        int epicCode = 1;
        String errorMessage = "Типы подзадач не совпадают с прогнозными";
        ArrayList<Task> currentList = new ArrayList<>();
        SubTask subTask = inMemoryTaskManager.subTasksList.get(1);
        epicAdmin.addToArray(currentList, subTask);
        Epic epic = inMemoryTaskManager.epicList.get(2);
        epicAdmin.addToArray(currentList, epic);
        epicAdmin.put(epicCode, currentList);
        currentList = epicAdmin.getEpicsSubTasksList(epicCode);
        boolean resoult = (currentList.get(0) instanceof SubTask) && (currentList.get(1) instanceof Epic);
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void testingIfAddObjectsInEpicCorrectlyWorks() {
        //для тестов генерируем пустой эпик
        inMemoryTaskManager.createEpic("Test epic", "Generated for epicAdmin testing", 0);
        int epicCode = inMemoryTaskManager.getEpicCounter();
        ArrayList<Task> currentList = new ArrayList<>();
        epicAdmin.put(epicCode, currentList);
        String errorMessage = "Подзадача не добавилась в пустой эпик.";
        SubTask subTask = inMemoryTaskManager.subTasksList.get(1);
        boolean resoult = epicAdmin.addSubTaskInEpic(epicCode, subTask, 5);
        assertEquals(true, resoult, errorMessage);

        //Добавляем еще две подзадачи
        subTask = inMemoryTaskManager.subTasksList.get(2);
        resoult = epicAdmin.addSubTaskInEpic(epicCode, subTask, 0);
        errorMessage = "Подзадача не добавилась в существующий список.";
        assertEquals(true, resoult, errorMessage);

        errorMessage = "Подзадача добавилась не в начало списка подзадач.";
        currentList = epicAdmin.getEpicsSubTasksList(epicCode);
        resoult = (currentList.get(0).code == 2);
        assertEquals(true, resoult, errorMessage);

        subTask = inMemoryTaskManager.subTasksList.get(3);
        epicAdmin.addSubTaskInEpic(epicCode, subTask, 1);
        errorMessage = "Подзадача добавилась не в середину списка подзадач.";
        currentList = epicAdmin.getEpicsSubTasksList(epicCode);
        resoult = (currentList.get(1).code == 3);
        assertEquals(true, resoult, errorMessage);

        int listSize = currentList.size();
        errorMessage = "Количество подзадач в списке не соответствует ожидаемому.";
        assertEquals(3, listSize, errorMessage);
    }

    @Test
    public void testingIfEpicCorrectlyAddAsSubTask() {
        //Вначале создаем два эпика и заносим в них по три подзадачи
        int epicCode1 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode1, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList1 = new ArrayList<>();
        epicAdmin.put(epicCode1,currentList1);
        SubTask subTask = inMemoryTaskManager.subTasksList.get(1);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 0);
        subTask = inMemoryTaskManager.subTasksList.get(2);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 1);
        subTask = inMemoryTaskManager.subTasksList.get(3);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 2);

        int epicCode2 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode2, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList2 = new ArrayList<>();
        epicAdmin.put(epicCode2,currentList2);
        subTask = inMemoryTaskManager.subTasksList.get(4);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 0);
        subTask = inMemoryTaskManager.subTasksList.get(5);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 1);
        subTask = inMemoryTaskManager.subTasksList.get(6);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 2);
        Epic epic1 = inMemoryTaskManager.epicList.get(epicCode1);
        boolean resoult = epicAdmin.addSubTaskInEpic(epicCode2, epic1, 2);
        String errorMessage = "Эпик " + epicCode1 + " не добавлен";
        assertEquals(true, resoult, errorMessage);
        currentList2 = epicAdmin.getEpicsSubTasksList(epicCode2);
        errorMessage = "Эпик " + epicCode1 + " не занесен на нужное место";
        String testString = "Test epic #" + epicCode1;
        resoult = (currentList2.get(2).name.equals(testString));
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void shouldNotAddEpicToOneselfAddAsSubTask() {
        //Вначале создаем два эпика и заносим в них по три подзадачи
        int epicCode1 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode1, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList1 = new ArrayList<>();
        epicAdmin.put(epicCode1,currentList1);
        SubTask subTask = inMemoryTaskManager.subTasksList.get(1);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 0);
        subTask = inMemoryTaskManager.subTasksList.get(2);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 1);
        subTask = inMemoryTaskManager.subTasksList.get(3);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 2);

        int epicCode2 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode2, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList2 = new ArrayList<>();
        epicAdmin.put(epicCode2,currentList2);
        subTask = inMemoryTaskManager.subTasksList.get(4);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 0);
        subTask = inMemoryTaskManager.subTasksList.get(5);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 1);
        subTask = inMemoryTaskManager.subTasksList.get(6);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 2);
        Epic epic1 = inMemoryTaskManager.epicList.get(epicCode1);
        epicAdmin.addSubTaskInEpic(epicCode2, epic1, 2);

        //Пробуем в эпик epicCode1 добавить его же как подзадачу
        boolean resoult = epicAdmin.addSubTaskInEpic(epicCode1, epic1, 0);
        String errorMessage = "Эпик " + epicCode1 + " добавлен к самому себе в качестве подзадачи";
        assertEquals(false, resoult, errorMessage);
    }

    @Test
    public void shouldNotAddEpicThanWouldRecursiveOwnSubTask() {
        //Тест для проверки опосредованного возникновения ситуации "Эпик является своей подзадачей".
        //В эпике epicCode2 есть подзадача эпик epicCode1. В эпик epicCode3 добавляем как подзадачу
        //эпик epicCode2 и пытаемся добавить эпик epicCode3 подзадачей в эпик epicCode1.
        //Получаем Эпик1 хочет взять подзадачей Эпик3, у которого подзадача Эпик2, у которого подзадача Эпик1.
        //Добавление должно быть запрещено
        //Вначале создаем два эпика и заносим в них по три подзадачи
        int epicCode1 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode1, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList1 = new ArrayList<>();
        epicAdmin.put(epicCode1,currentList1);
        SubTask subTask = inMemoryTaskManager.subTasksList.get(1);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 0);
        subTask = inMemoryTaskManager.subTasksList.get(2);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 1);
        subTask = inMemoryTaskManager.subTasksList.get(3);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 2);

        int epicCode2 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode2, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList2 = new ArrayList<>();
        epicAdmin.put(epicCode2,currentList2);
        subTask = inMemoryTaskManager.subTasksList.get(4);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 0);
        subTask = inMemoryTaskManager.subTasksList.get(5);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 1);
        subTask = inMemoryTaskManager.subTasksList.get(6);
        epicAdmin.addSubTaskInEpic(epicCode2, subTask, 2);
        Epic epic1 = inMemoryTaskManager.epicList.get(epicCode1);
        //Epic1 добавляем подзадачей в Epic2
        epicAdmin.addSubTaskInEpic(epicCode2, epic1, 0);

        int epicCode3 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode3, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList3 = new ArrayList<>();
        epicAdmin.put(epicCode3,currentList3);
        Epic epic2 = inMemoryTaskManager.epicList.get(epicCode2);
        Epic epic3 = inMemoryTaskManager.epicList.get(epicCode3);
        //Epic2 добавляем подзадачей в Epic3
        epicAdmin.addSubTaskInEpic(epicCode3, epic2, 0);
        //И пытаемся Epic3 добавить подзадачей в Epic1
        boolean resoult = epicAdmin.addSubTaskInEpic(epicCode1, epic3, 0);
        String errorMessage = "Эпик " + epicCode1 + " опосредованно добавлен к самому себе в качестве подзадачи";
        assertEquals(false, resoult, errorMessage);
    }

    @Test
    public void shouldEpicAdminClearHisMap() {
        //для проверки создаем epic и заносим его в Map
        int epicCode1 = inMemoryTaskManager.getEpicCounter() + 1;
        inMemoryTaskManager.createEpic("Test epic #" + epicCode1, "Generated for epicAdmin testing", 0);
        ArrayList<Task> currentList1 = new ArrayList<>();
        epicAdmin.put(epicCode1,currentList1);
        SubTask subTask = inMemoryTaskManager.subTasksList.get(1);
        epicAdmin.addSubTaskInEpic(epicCode1, subTask, 0);
        boolean resoult = epicAdmin.isEpicAdminEmpty();
        String errorMessage = "EpicAdmin в данный момент не пуст";
        assertEquals(false, resoult, errorMessage);
        epicAdmin.deleteAllEpics();
        resoult = epicAdmin.isEpicAdminEmpty();
        errorMessage = "EpicAdmin в данный момент пуст.";
        assertEquals(true, resoult, errorMessage);
    }
}