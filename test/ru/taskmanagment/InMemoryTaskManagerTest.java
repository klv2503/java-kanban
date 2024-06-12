package ru.taskmanagment;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    static InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
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
    public void shouldCreateSubTaskInEpicAndFindAfter() {
        int epicId = inMemoryTaskManager.getSpecificEpic();
        int place = 1;
        if (epicId == -1) {
            epicId = 1;
            place = 0;
        }
        int taskId = 6;
        String errorMessage = "Данные найденной подзадачи не совпадают с данными задачи-прототипа";
        Task task = inMemoryTaskManager.getTaskWithId(6);
        inMemoryTaskManager.addNewSubToEpic(epicId, taskId, place);
        SubTask subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(epicId, place);
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

    // Блок тестирования HistoryManager
    @Test
    public void testingMaximalSizeOfHistoryManagersArray() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = inMemoryTaskManager.getTaskWithId(i + 1);
            epic = inMemoryTaskManager.getEpicByCode(i + 1);
            subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(i + 1, 0);
        }
        int arraySize = inMemoryTaskManager.inMemoryHistoryManager.getHistory().size();
        assertEquals(10, arraySize, "Size of HistoryManagers Array is not equal to 10.");
    }

    @Test
    public void testingHistoryManagersArrayKeepsDifferentTypes() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = inMemoryTaskManager.getTaskWithId(i + 1);
            epic = inMemoryTaskManager.getEpicByCode(i + 1);
            subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(i + 1, 1);
        }
        ArrayList<Task> currentArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
        boolean haveTask = false;
        boolean haveSubTask = false;
        boolean haveEpic = false;
        for (int i = 0; i < currentArray.size(); i++) {
            haveTask = haveTask || (currentArray.get(i) instanceof Task);
            haveSubTask = haveSubTask || (currentArray.get(i) instanceof SubTask);
            haveEpic = haveEpic || (currentArray.get(i) instanceof Epic);
        }
        String errorMessage = "HistoryManager keep not Task";
        assertEquals(true, haveTask, errorMessage);
        errorMessage = "HistoryManager keep not SubTask";
        assertEquals(true, haveSubTask, errorMessage);
        errorMessage = "HistoryManager keep not Epic";
        assertEquals(true, haveEpic, errorMessage);
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
}