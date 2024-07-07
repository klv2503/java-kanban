package ru.taskmanagment;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    static InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
    static int numberOfGeneratedTasks = 10;
    static int numberOfGeneratedEpics = 6;
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
        boolean tstBool = taskManager.getHowSubTasks(2) > 0;
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
        boolean tstBool = inMemoryTaskManager.tasksList.containsKey(8);
        assertTrue(tstBool, "Не нашли задачу № 8, которая должна быть в списке");
    }

    @Test
    public void shouldNotFindTask50() {
        boolean tstBool = inMemoryTaskManager.tasksList.containsKey(50);
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


    @Test
    public void shouldTestDeletingSubTasksWithTask() {
        //Генерация тестовых Задачи и подзадач с занесением их в историю
        int bound = inMemoryTaskManager.getTaskCounter();
        int id = rnd.nextInt(bound);
        Task task = inMemoryTaskManager.getTaskWithId(id);
        while (task == null) {
            id = rnd.nextInt(bound);
            task = inMemoryTaskManager.getTaskWithId(id);
        }
        ArrayList<String> indexInHistory = new ArrayList<>();
        indexInHistory.add("t" + id);
        for (int i : inMemoryTaskManager.epicsList.keySet()) {
            Epic epic = inMemoryTaskManager.epicsList.get(i);
            SubTask subTask = inMemoryTaskManager.makeSubTask(task);
            indexInHistory.add("s" + subTask.ownCode);
            epic.addSubTaskInEpic(subTask, 0);
            inMemoryTaskManager.getEpicsSubTaskByIndex(i, 0);
            inMemoryTaskManager.epicsList.put(i, epic);
        }
        //Теперь удаляем задачу. Все ее подзадачи должны быть удалены из списка подзадач и из всех эпиков.
        // Она и ее подзадачи также должны быть удалены из истории
        inMemoryTaskManager.removeTaskWithId(id);
        boolean isExist = inMemoryTaskManager.tasksList.containsKey(id);
        for (int i : inMemoryTaskManager.subTasksList.keySet()) {
            isExist = isExist || (inMemoryTaskManager.subTasksList.get(i).code == id);
        }
        String errorMessage = "Task or SubTask didn't deleted from List";
        assertEquals(false, isExist, errorMessage);
        for (int i : inMemoryTaskManager.epicsList.keySet()) {
            ArrayList<SubTask> workArray = inMemoryTaskManager.epicsList.get(i).epicsTasks;
            for (int j = 0; j < workArray.size(); j++) {
                SubTask subTask = workArray.get(j);
                isExist = isExist || (subTask.code == id);
            }
        }
        errorMessage = "Epics have SubTask with deleted Task";
        assertEquals(false, isExist, errorMessage);
        //Проверяем, удалены ли все экземпляры из истории
        isExist = inMemoryTaskManager.inMemoryHistoryManager.history.containsKey("t" + id);
        for (String hisCode : indexInHistory) {
            isExist = isExist || inMemoryTaskManager.inMemoryHistoryManager.history.containsKey(hisCode);
        }
        errorMessage = "History has deleted Task or SubTasks";
        assertEquals(false, isExist, errorMessage);
    }

    // Блок тестирования HistoryManager
    @Test
    public void testingIfMaximalSizeOfHistoryCanBeBiggerThan10() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 6; i++) {
            task = inMemoryTaskManager.getTaskWithId(i + 1);
            epic = inMemoryTaskManager.getEpicByCode(i + 1);
            subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(i + 1, 0);
        }
        boolean isLonger10 = 10 < inMemoryTaskManager.inMemoryHistoryManager.getHistory().size();
        assertTrue(isLonger10, "Size of HistoryManagers Array is not bigger than 10.");
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
    public void testingIfHistoryCanHaveTwiceOneObject() {
        Task task;
        SubTask subTask;
        Epic epic;
        for (int i = 0; i < 4; i++) {
            task = inMemoryTaskManager.getTaskWithId(i + 1);
            epic = inMemoryTaskManager.getEpicByCode(i + 1);
            subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(i + 1, 1);
        }
        // Вызываем get для объектов, которые только что занесены в историю
        subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(1, 1);
        epic = inMemoryTaskManager.getEpicByCode(2);
        task = inMemoryTaskManager.getTaskWithId(3);

        int counterSub = 0;
        int counterEpic = 0;
        int counterTask = 0;
        ArrayList<Task> currentArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
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
        errorMessage = "HistoryManager keep twice SUbTask #1 of Epic #1";
        assertEquals(1, counterSub, errorMessage);
        errorMessage = "HistoryManager keep twice Epic #2";
        assertEquals(1, counterEpic, errorMessage);
    }

    @Test
    public void shouldTestIfTaskAndAllSubTasksAreInHistory() {
        //Генерация данных для теста
        int bound = inMemoryTaskManager.getTaskCounter();
        int id = rnd.nextInt(bound);
        ArrayList<String> indexes = new ArrayList<>();
        Task task = inMemoryTaskManager.getTaskWithId(id);
        while (task == null) {
            id = rnd.nextInt(bound);
            task = inMemoryTaskManager.getTaskWithId(id);
        }
        indexes.add("t" + id);

        for (int i : inMemoryTaskManager.epicsList.keySet()) {
            Epic epic = inMemoryTaskManager.epicsList.get(i);
            SubTask subTask = inMemoryTaskManager.makeSubTask(task);
            epic.addSubTaskInEpic(subTask, 0);
            subTask = inMemoryTaskManager.getEpicsSubTaskByIndex(i, 0);
            indexes.add("s" + subTask.ownCode);
        }
        //Проверка, находятся ли Task и все сгенерированные для теста SubTask в истории
        boolean isSaved = true;
        for (String i : indexes) {
            isSaved = isSaved && inMemoryTaskManager.inMemoryHistoryManager.history.containsKey(i);
        }
        String errorMessage = "HistoryManager keep not Task or not all SubTasks";
        assertEquals(true, isSaved, errorMessage);
    }

    @Test
    public void shouldCorrectAddTaskInClearHistoryAndDeleteSingleTask() {
        //Чистим историю и проверяем, получилось ли
        inMemoryTaskManager.inMemoryHistoryManager.removeAllHistory();
        ArrayList<Task> workArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
        boolean isClear = workArray.isEmpty();
        String errorMessage = "History must be empty, but has elements";
        assertEquals(true, isClear, errorMessage);
        //Добавляем в историю одну задачу и проверяем, не осталась ли история пустой
        int bound = inMemoryTaskManager.getTaskCounter();
        int id = rnd.nextInt(bound);
        Task task = inMemoryTaskManager.getTaskWithId(id);
        while (task == null) {
            id = rnd.nextInt(bound);
            task = inMemoryTaskManager.getTaskWithId(id);
        }
        workArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
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
        boolean isCorrectTask = (workArray.get(0).equals(inMemoryTaskManager.tasksList.get(id)));
        errorMessage = "In History written another Task";
        assertEquals(true, isCorrectTask, errorMessage);
        //Удаляем единственную задачу из истории
        inMemoryTaskManager.inMemoryHistoryManager.remove("t" + id);
        workArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
        isClear = workArray.isEmpty();
        errorMessage = "History must be empty again, but has elements";
        assertEquals(true, isClear, errorMessage);
    }

    @Test
    public void shouldHistoryCorrectDeleteTaskAndAddAfterSecondGet() {
// Очищаем историю и заполняем ее тестовыми задачами
        ArrayList<Task> taskForTest = new ArrayList<>();
        inMemoryTaskManager.inMemoryHistoryManager.removeAllHistory();
        for (int i = 0; i < 10; i++) {
            inMemoryTaskManager.createTask("TestName" + i, "TestDesc" + i);
            Task task = inMemoryTaskManager.getTaskWithId(inMemoryTaskManager.getTaskCounter());
            taskForTest.add(task);
        }
        //Удаляем задачу TestName2 (из середины списка)
        int id = taskForTest.get(2).code;
        inMemoryTaskManager.removeTaskWithId(id);
        ArrayList<Task> workArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
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
        inMemoryTaskManager.removeTaskWithId(id);
        workArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
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
        inMemoryTaskManager.getTaskWithId(taskForTest.get(0).code);
        workArray = inMemoryTaskManager.inMemoryHistoryManager.getHistory();
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