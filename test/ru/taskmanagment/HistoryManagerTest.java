package ru.taskmanagment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HistoryManagerTest extends TaskManagerTest {

    static InMemoryTaskManager manager = new InMemoryTaskManager();
    static Random rnd = new Random();

    @BeforeEach
    public void generation() {
        dataGeneration(manager);
    }

    @AfterEach
    public void clearing() {
        clearData(manager);
    }

    @Test
    public void testingIfMaximalSizeOfHistoryCanBeBiggerThan10() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 6; i++) {
            task = manager.getTaskWithId(i + 1);
            epic = manager.getEpicByCode(i + 1);
            subTask = manager.getEpicsSubTaskByIndex(i + 1, 0);
        }
        boolean isLonger10 = 10 < manager.inMemoryHistoryManager.getHistory().size();
        assertTrue(isLonger10, "Size of HistoryManagers Array is not bigger than 10.");
    }

    @Test
    public void testingHistoryManagersArrayKeepsDifferentTypes() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = manager.getTaskWithId(i + 1);
            epic = manager.getEpicByCode(i + 1);
            subTask = manager.getEpicsSubTaskByIndex(i + 1, 1);
        }
        ArrayList<Task> currentArray = manager.inMemoryHistoryManager.getHistory();
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
    public void testingIfHistoryCanHaveTwiceOneObject() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = manager.getTaskWithId(i + 1);
            epic = manager.getEpicByCode(i + 1);
            subTask = manager.getEpicsSubTaskByIndex(i + 1, 1);
        }
        // Вызываем get для объектов, которые только что занесены в историю
        subTask = manager.getEpicsSubTaskByIndex(1, 1);
        epic = manager.getEpicByCode(2);
        task = manager.getTaskWithId(3);

        int counterSub = 0;
        int counterEpic = 0;
        int counterTask = 0;
        ArrayList<Task> currentArray = manager.inMemoryHistoryManager.getHistory();
        for (int i = 0; i < currentArray.size(); i++) {
            if (currentArray.get(i) instanceof SubTask) {
                if (((SubTask) currentArray.get(i)).ownCode == subTask.ownCode)
                    counterSub++;
            }
            if (currentArray.get(i) instanceof Epic) {
                if (currentArray.get(i).code == epic.code)
                    counterEpic++;
            }
            if (!(currentArray.get(i) instanceof SubTask) && !(currentArray.get(i) instanceof Epic)) {
                if (currentArray.get(i).code == task.code)
                    counterTask++;
            }
        }
        String errorMessage = "HistoryManager keep twice Task #3";
        assertEquals(1, counterTask, errorMessage);
        errorMessage = "HistoryManager keep twice SubTask #1 of Epic #1";
        assertEquals(1, counterSub, errorMessage);
        errorMessage = "HistoryManager keep twice Epic #2";
        assertEquals(1, counterEpic, errorMessage);
    }

    @Test
    public void shouldTestIfTaskAndAllSubTasksAreInHistory() {
        //Генерация данных для теста
        int bound = manager.getTaskCounter();
        int id = rnd.nextInt(bound);
        ArrayList<String> indexes = new ArrayList<>();
        Task task = manager.getTaskWithId(id);
        while (task == null) {
            id = rnd.nextInt(bound);
            task = manager.getTaskWithId(id);
        }
        indexes.add("t" + id);

        for (int i : manager.epicsList.keySet()) {
            Epic epic = manager.epicsList.get(i);
            SubTask subTask = manager.makeSubTask(task, manager.standartDuration);
            epic.addSubTaskInEpic(subTask);
            subTask = manager.getEpicsSubTaskByIndex(i, 0);
            indexes.add("s" + subTask.ownCode);
        }
        //Проверка, находятся ли Task и все сгенерированные для теста SubTask в истории
        boolean isSaved = true;
        for (String i : indexes) {
            isSaved = isSaved && manager.inMemoryHistoryManager.history.containsKey(i);
        }
        String errorMessage = "HistoryManager keep not Task or not all SubTasks";
        assertEquals(true, isSaved, errorMessage);
    }

    @Test
    public void shouldCorrectAddTaskInClearHistoryAndDeleteSingleTask() {
        //Чистим историю и проверяем, получилось ли
        manager.inMemoryHistoryManager.removeAllHistory();
        ArrayList<Task> workArray = manager.inMemoryHistoryManager.getHistory();
        boolean isClear = workArray.isEmpty();
        String errorMessage = "History must be empty, but has elements";
        assertEquals(true, isClear, errorMessage);
        //Добавляем в историю одну задачу и проверяем, не осталась ли история пустой
        int bound = manager.getTaskCounter();
        int id = rnd.nextInt(bound);
        Task task = manager.getTaskWithId(id);
        while (task == null) {
            id = rnd.nextInt(bound);
            task = manager.getTaskWithId(id);
        }
        workArray = manager.inMemoryHistoryManager.getHistory();
        isClear = workArray.isEmpty();
        errorMessage = "History must have one element, but is empty";
        assertEquals(false, isClear, errorMessage);
        //Проверяем, что в истории ровно один элемент
        boolean isOnlyOne = false;
        if (!workArray.isEmpty()) {
            isOnlyOne = (workArray.size() == 1);
        }
        errorMessage = "History must have only one element, but has more";
        assertEquals(true, isOnlyOne, errorMessage);
        //Проверяем, действительно ли в историю записана именно запрошенная задача
        boolean isCorrectTask = (workArray.get(0).equals(manager.tasksList.get(id)));
        errorMessage = "In History written another Task";
        assertEquals(true, isCorrectTask, errorMessage);
        //Удаляем единственную задачу из истории
        manager.inMemoryHistoryManager.remove("t" + id);
        workArray = manager.inMemoryHistoryManager.getHistory();
        isClear = workArray.isEmpty();
        errorMessage = "History must be empty again, but has elements";
        assertEquals(true, isClear, errorMessage);
    }

    @Test
    public void shouldHistoryCorrectDeleteTaskAndAddAfterSecondGet() {
// Очищаем историю и заполняем ее тестовыми задачами
        ArrayList<Task> taskForTest = new ArrayList<>();
        manager.inMemoryHistoryManager.removeAllHistory();
        for (int i = 0; i < 10; i++) {
            manager.createTask("TestName" + i, "TestDesc" + i);
            Task task = manager.getTaskWithId(manager.getTaskCounter());
            taskForTest.add(task);
        }
        //Удаляем задачу TestName2 (из середины списка)
        int id = taskForTest.get(2).code;
        manager.removeTaskWithId(id);
        ArrayList<Task> workArray = manager.inMemoryHistoryManager.getHistory();
        boolean isCorrect = !workArray.isEmpty();
        if (isCorrect) {
            isCorrect = isCorrect && (workArray.size() == 9);
        }
        String errorMessage = "History must have 9 elements";
        assertEquals(true, isCorrect, errorMessage);
        isCorrect = (workArray.get(0).equals(taskForTest.get(0))) &&
                (workArray.get(1).equals(taskForTest.get(1)));
        for (int i = 2; i < 9; i++) {
            isCorrect = isCorrect && (workArray.get(i).equals(taskForTest.get(i + 1)));
        }
        errorMessage = "Unexpected History";
        assertEquals(true, isCorrect, errorMessage);

        //Теперь удаляем задачу из хвоста списка
        id = taskForTest.get(9).code;
        manager.removeTaskWithId(id);
        workArray = manager.inMemoryHistoryManager.getHistory();
        isCorrect = !workArray.isEmpty();
        if (isCorrect) {
            isCorrect = isCorrect && (workArray.size() == 8);
        }
        errorMessage = "History must have 8 elements";
        assertEquals(true, isCorrect, errorMessage);
        isCorrect = (workArray.get(0).equals(taskForTest.get(0))) &&
                (workArray.get(1).equals(taskForTest.get(1)));
        for (int i = 2; i < 8; i++) {
            isCorrect = isCorrect && (workArray.get(i).equals(taskForTest.get(i + 1)));
        }
        errorMessage = "Unexpected History after deleting from tail";
        assertEquals(true, isCorrect, errorMessage);

        //Повторно просматриваем задачу TestName0
        manager.getTaskWithId(taskForTest.get(0).code);
        workArray = manager.inMemoryHistoryManager.getHistory();
        isCorrect = !workArray.isEmpty();
        if (isCorrect) {
            isCorrect = isCorrect && (workArray.size() == 8);
        }
        errorMessage = "History must have 8 elements";
        assertEquals(true, isCorrect, errorMessage);
        isCorrect = (workArray.get(7).equals(taskForTest.get(0))) &&
                (workArray.get(0).equals(taskForTest.get(1)));
        for (int i = 2; i < 7; i++) {
            isCorrect = isCorrect && (workArray.get(i).equals(taskForTest.get(i + 2)));
        }
        errorMessage = "Unexpected History after second GetTask";
        assertEquals(true, isCorrect, errorMessage);
    }
}
