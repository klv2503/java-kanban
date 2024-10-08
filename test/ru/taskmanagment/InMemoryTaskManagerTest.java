package ru.taskmanagment;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest {

    static InMemoryTaskManager manager = new InMemoryTaskManager();

    @BeforeEach
    public void generation() {
        dataGeneration(manager);
    }

    @AfterEach
    public void clearing() {
        clearData(manager);
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
        manager.createTask(name, description);
        int index = manager.getTaskCounter();
        Task task = manager.getTaskWithId(index);
        boolean resoult = task.name.equals(name) && task.description.equals(description);
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void shouldCreateEpicAndFindAfter() {
        String name = "Epic creation";
        String description = "New epic for testing of creation and seeking";
        String errorMessage = "Данные найденного эпика не совпадают с данными только что созданного";
        manager.createEpic(name, description, 0);
        int index = manager.getEpicCounter();
        Epic epic = manager.getEpicByCode(index);
        boolean resoult = epic.name.equals(name) && epic.description.equals(description);
        assertEquals(true, resoult, errorMessage);
    }

    @Test
    public void shouldCreateSubTaskInEpicAndFindAfter() {
        int epicId = manager.getSpecificEpic();
        int quantitySubTask = manager.epicsList.get(epicId).getEpicsTasks().size();
        int taskId = 6;
        String errorMessage = "Данные найденной подзадачи не совпадают с данными задачи-прототипа";
        Task task = manager.getTaskWithId(6);
        manager.addNewSubToEpic(epicId, manager.makeSubTask(manager.tasksList.get(taskId), 15));
        SubTask subTask = manager.getEpicsSubTaskByIndex(epicId, quantitySubTask);
        boolean result = subTask.name.equals(task.name) && subTask.description.equals(task.description);
        assertEquals(true, result, errorMessage);
    }

    @Test
    public void shouldFindExistingTask8() {
        boolean tstBool = manager.tasksList.containsKey(8);
        assertTrue(tstBool, "Не нашли задачу № 8, которая должна быть в списке");
    }

    @Test
    public void shouldNotFindTask50() {
        boolean tstBool = manager.tasksList.containsKey(50);
        assertFalse(tstBool, "Задача № 50 найдена, хотя ее не должно быть.");
    }

    @Test
    public void shouldTestThatMethodReturnsNullThenTaskNotExist() {
        int id = numberOfGeneratedTasks + 25;
        Task task = manager.getTaskWithId(id);
        assertNull(task, "Task № " + id + " is not equal to null.");
    }

    @Test
    public void shouldCompareTask1AndTask2() {
        int bound = manager.getTaskCounter();
        int id1 = rnd.nextInt(bound);
        Task task1 = manager.getTaskWithId(id1);
        while (task1 == null) {
            id1 = rnd.nextInt(bound);
            task1 = manager.getTaskWithId(id1);
        }
        int id2 = rnd.nextInt(bound);
        Task task2 = manager.getTaskWithId(id2);
        while (task2 == null) {
            id2 = rnd.nextInt(bound);
            task2 = manager.getTaskWithId(id2);
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
        int bound = manager.getTaskCounter();
        int id = rnd.nextInt(bound);
        Task task = manager.getTaskWithId(id);
        while (task == null) {
            id = rnd.nextInt(bound);
            task = manager.getTaskWithId(id);
        }
        ArrayList<String> indexInHistory = new ArrayList<>();
        indexInHistory.add("t" + id);
        for (int i : manager.epicsList.keySet()) {
            Epic epic = manager.epicsList.get(i);
            SubTask subTask = manager.makeSubTask(task, manager.standartDuration);
            indexInHistory.add("s" + subTask.ownCode);
            epic.addSubTaskInEpic(subTask);
            manager.getEpicsSubTaskByIndex(i, 0);
            manager.epicsList.put(i, epic);
        }
        //Теперь удаляем задачу. Все ее подзадачи должны быть удалены из списка подзадач и из всех эпиков.
        // Она и ее подзадачи также должны быть удалены из истории
        manager.removeTaskWithId(id);
        boolean isExist = manager.tasksList.containsKey(id);
        for (int i : manager.subTasksList.keySet()) {
            isExist = isExist || (manager.subTasksList.get(i).code == id);
        }
        String errorMessage = "Task or SubTask didn't deleted from List";
        assertEquals(false, isExist, errorMessage);
        for (int i : manager.epicsList.keySet()) {
            ArrayList<SubTask> workArray = manager.epicsList.get(i).epicsTasks;
            for (int j = 0; j < workArray.size(); j++) {
                SubTask subTask = workArray.get(j);
                isExist = isExist || (subTask.code == id);
            }
        }
        errorMessage = "Epics have SubTask with deleted Task";
        assertEquals(false, isExist, errorMessage);
        //Проверяем, удалены ли все экземпляры из истории
        isExist = manager.inMemoryHistoryManager.history.containsKey("t" + id);
        for (String hisCode : indexInHistory) {
            isExist = isExist || manager.inMemoryHistoryManager.history.containsKey(hisCode);
        }
        errorMessage = "History has deleted Task or SubTasks";
        assertEquals(false, isExist, errorMessage);
    }

    @Test
    public void shouldTestingEpicsStatus() {
        //Проверяем, как определяется статус эпика для возможных ситуаций
        // создаем эпик с пустым набором подзадач, его статус должен быть NEW
        Epic epic = manager.createEpic("testName", "testDescription", 0);
        boolean isNew = epic.getStatus().equals(Status.NEW);
        String errorMessage = "Статус эпика с пустым набором подзадач не NEW";
        assertEquals(true, isNew, errorMessage);
        //теперь добавляем эпику 8 подзадач со статусом NEW, статус эпика должен остаться NEW
        epic.epicsTasks = manager.createEpicSubTasks(8);
        isNew = epic.getStatus().equals(Status.NEW);
        errorMessage = "Статусы всех подзадач эпика NEW, а статус эпика не NEW";
        assertEquals(true, isNew, errorMessage);
        //у первых трех подзадач меняем статус на DONE, статус эпика должен поменяться на IN_PROGRESS
        for (int i = 0; i < 3; i++) {
            manager.changeSubTaskStatus(epic.getCode(), i, Status.DONE);
        }
        boolean isInProgress = epic.getStatus().equals(Status.IN_PROGRESS);
        errorMessage = "1.Статус эпика должен быть IN_PROGRESS";
        assertEquals(true, isInProgress, errorMessage);
        //у первых следующих двух подзадач меняем статус на IN_PROGRESS, статус эпика должен остаться IN_PROGRESS
        manager.changeSubTaskStatus(epic.getCode(), 3, Status.IN_PROGRESS);
        manager.changeSubTaskStatus(epic.getCode(), 4, Status.IN_PROGRESS);
        errorMessage = "2.Статус эпика должен быть IN_PROGRESS";
        isInProgress = epic.getStatus().equals(Status.IN_PROGRESS);
        assertEquals(true, isInProgress, errorMessage);
        //Меняем статусы всех подзадач эпика на DONE, статус эпика тоже должен стать DONE
        for (int i = 3; i < 8; i++) {
            manager.changeSubTaskStatus(epic.getCode(), i, Status.DONE);
        }
        boolean isDone = epic.getStatus().equals(Status.DONE);
        errorMessage = "Статус эпика должен быть DONE";
        assertEquals(true, isDone, errorMessage);
    }

    @Test
    public void shouldBeEpicForEachSubTask() {
        //Подзадачи генерируются как элементы списков в эпиках. Теоретически позадача без эпика
        //может возникнуть в результате удаления эпиков. Удаляем пару эпиков после генерации и проверяем
        //Но вначале проверяем, действительно ли у нас после генерации есть эпики и подзадачи
        boolean isCorrectSituation = !manager.epicsList.isEmpty();
        String errorMessage = "После генерации получили пустой список эпиков";
        assertEquals(true, isCorrectSituation, errorMessage);
        isCorrectSituation = !manager.subTasksList.isEmpty();
        errorMessage = "После генерации получили пустой список подзадач";
        assertEquals(true, isCorrectSituation, errorMessage);

        int bound = manager.getEpicCounter();
        int id = rnd.nextInt(bound);
        manager.epicsList.remove(id);
        id = rnd.nextInt(bound);
        manager.epicsList.remove(id);
        isCorrectSituation = false;
        for (int i : manager.subTasksList.keySet()) {
            SubTask subTask = manager.subTasksList.get(i);
            boolean isEpicExists = false;
            for (int j : manager.epicsList.keySet()) {
                isEpicExists = isEpicExists || manager.epicsList.get(j).getEpicsTasks().contains(subTask);
            }
            if (!isEpicExists)
                errorMessage = "Подзадача " + subTask.ownCode + " не принадлежит ни к одному эпику";
            isCorrectSituation = isCorrectSituation || isEpicExists;
        }
        assertEquals(true, isCorrectSituation, errorMessage);
    }

    @Test
    public void shouldCorrectDefineIfSubTaskIsValidByTime() {
        manager.getPrioritizedTasks();
        Iterator<Task> iter = manager.sortedTasks.iterator();
        //Определяем время и дату старта самой первой подзадачи
        LocalDateTime firstStart = iter.next().startTime;
        Task workSub = null;
        while (iter.hasNext())
            workSub = iter.next();
        //...и самой последней
        LocalDateTime lastStart = workSub.getStartTime();
        LocalDateTime lastEnd = workSub.getEndTime();
        //создаем подзадачу, которая начинается и заканчивается до начала самой ранней из сгенерированных
        //основой тестовых подзадач выбираем самую первую сгенерированную, что не принципиально
        SubTask testedSub = new SubTask(manager.tasksList.get(1), 100,
                Status.NEW, 15, firstStart.minusMinutes(20));
        boolean isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        String errorMessage = "1. Ошибочно объявлено, что время подзадачи занято"
                + " tested startTime = " + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: firstStart = " + firstStart.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Меняем продолжительность подзадачи так, ее окончание совпало с началом самой ранней сгенерированной
        testedSub.setDuration(Duration.ofMinutes(20));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "2. Ошибочно объявлено, что время подзадачи занято"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: firstStart = " + firstStart.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Меняем продолжительность подзадачи так, чтобы она наложилась на самую раннюю и снова проверяем
        testedSub.setDuration(Duration.ofMinutes(25));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "3. Ошибочно объявлено, что время подзадачи допустимо"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: firstStart = " + firstStart.format(TimeManager.dateTimeFormatter);
        assertEquals(false, isValid, errorMessage);
        //Меняем старт подзадачи так, чтобы она началась за 5 минут до окончания самой поздней сгенерированной
        testedSub.setStartTime(lastEnd.minusMinutes(5));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "4. Ошибочно объявлено, что время подзадачи допустимо"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: lastStart = " + lastStart.format(TimeManager.dateTimeFormatter);
        assertEquals(false, isValid, errorMessage);
        //Переносим старт подзадачи, чтобы он совпал со временем окончания самой поздней сгенерированной
        testedSub.setStartTime(lastEnd);
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "5. Ошибочно объявлено, что время подзадачи занято"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: lastEnd = " + lastEnd.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Еще немного откладываем старт подзадачи. На всякий случай
        testedSub.setStartTime(lastEnd.plusMinutes(1));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "6. Ошибочно объявлено, что время подзадачи занято"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: lastEnd = " + lastEnd.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Теперь выберем эпик и удалим его вместе с его подзадачами. Внутри выполнения появится свободное место.
        //Попытаемся это место занять
        LocalDateTime middleStart = manager.getEpicByCode(3).getStartTime();
        LocalDateTime middleEnd = manager.getEpicByCode(3).getEndTime();
        manager.deleteEpic(3);
        //У эпика могла быть ровно одна подзадача продолжительностью 15 минут. Сделаем продолжительность меньше 15 и
        //будем регулировать допустимость временем старта
        testedSub.setDuration(Duration.ofMinutes(10));
        //Вначале захватим часть времени подзадачи, которая выполняется до старта удаленного эпика.
        testedSub.setStartTime(middleStart.minusMinutes(5));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "7. Ошибочно объявлено, что время подзадачи допустимо"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: middleStart = " + middleStart.format(TimeManager.dateTimeFormatter);
        assertEquals(false, isValid, errorMessage);
        //Совместим старт тестовой подзадачи со стартом удаленного эпика
        testedSub.setStartTime(middleStart);
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "8. Ошибочно объявлено, что время подзадачи занято"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: middleStart = " + middleStart.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Помещаем выполнение тестовой подзадачи внутрь времени, ранее занятого удаленным эпиком
        testedSub.setStartTime(middleStart.plusMinutes(2));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "9. Ошибочно объявлено, что время подзадачи занято"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: middleStart = " + middleStart.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Теперь совместим финиш тестовой подзадачи с финишем удаленного эпика
        testedSub.setStartTime(middleEnd.minusMinutes(10));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "10. Ошибочно объявлено, что время подзадачи занято"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: middleEnd = " + middleEnd.format(TimeManager.dateTimeFormatter);
        assertEquals(true, isValid, errorMessage);
        //Сдвигаем старт так, чтобы высвободившегося времени не хватило и возникло пересечение
        //со временем следующей подзадачи
        testedSub.setStartTime(middleEnd.minusMinutes(9));
        isValid = manager.isValidTime(testedSub.startTime, testedSub.duration);
        errorMessage = "11. Ошибочно объявлено, что время подзадачи допустимо"
                + testedSub.startTime.format(TimeManager.dateTimeFormatter) + ";"
                + " duration = " + TimeManager.duration2String(testedSub.duration) + ";"
                + " data for test: middleEnd = " + middleEnd.format(TimeManager.dateTimeFormatter);
        assertEquals(false, isValid, errorMessage);
    }

    @Test
    public void shouldCorrectCompareTwoTasksByTime() {
        //Создаем тестовые task и subTask и проверяем работу метода isTasksNotCrossByTime в разных ситуациях.
        //Вначале task заканчивает выполнение до старта subTask
        Task task = new Task(30, "noName", Status.NEW, "noDescription",
                "10:00 02-09-2024", "00:15");
        SubTask subTask = new SubTask(50, "noName", Status.NEW, "noDescription",
                "11:00 02-09-2024", "00:15", 25);
        String errorMessage = "1. Ошибочно объявлено, что задачи наложились во времени";
        boolean isCross;
        isCross = manager.isTasksNotCrossByTime(task, subTask);
        assertTrue(isCross, errorMessage);
        //Совмещаем окончание выполнения task со стартом subTask. Это не считается наложением
        task.setStartTime(task.getStartTime().plusMinutes(45));
        errorMessage = "2. Ошибочно объявлено, что задачи наложились во времени";
        isCross = manager.isTasksNotCrossByTime(subTask, task);
        assertTrue(isCross, errorMessage);
        //Теперь task начинается до старта subTask, но заканчивается уже после стартового времени subTask.
        //Это запрещенная ситуация
        task.setStartTime(task.getStartTime().plusMinutes(10));
        errorMessage = "3. Ошибочно объявлено, что задачи не пересекаются во времени";
        isCross = manager.isTasksNotCrossByTime(subTask, task);
        assertFalse(isCross, errorMessage);
        //Совмещаем стартовое время у тестовых объектов. Это тоже запрещено
        task.setStartTime(task.getStartTime().plusMinutes(5));
        errorMessage = "4. Ошибочно объявлено, что задачи не пересекаются во времени";
        isCross = manager.isTasksNotCrossByTime(task, subTask);
        assertFalse(isCross, errorMessage);
        //Помещаем task целиком внутрь subTask. Это тоже запрещено
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(task.getStartTime().plusMinutes(1));
        errorMessage = "5. Ошибочно объявлено, что задачи не пересекаются во времени";
        isCross = manager.isTasksNotCrossByTime(task, subTask);
        assertFalse(isCross, errorMessage);
        //Теперь task начинается во время исполнения subTask, а заканчивается после ее окончания. Но и это запрещено
        task.setDuration(Duration.ofMinutes(15));
        task.setStartTime(task.getStartTime().plusMinutes(9));
        errorMessage = "6. Ошибочно объявлено, что задачи не пересекаются во времени";
        isCross = manager.isTasksNotCrossByTime(task, subTask);
        assertFalse(isCross, errorMessage);
        //Совмещаем старт task с окончанием исполнения subTask. Это не считается наложением
        task.setStartTime(task.getStartTime().plusMinutes(5));
        errorMessage = "7. Ошибочно объявлено, что задачи пересекаются во времени";
        isCross = manager.isTasksNotCrossByTime(subTask, task);
        assertTrue(isCross, errorMessage);
        //Теперь старт task наступает после окончания исполнения subTask.
        task.setStartTime(task.getStartTime().plusMinutes(1));
        errorMessage = "8. Ошибочно объявлено, что задачи пересекаются во времени";
        isCross = manager.isTasksNotCrossByTime(subTask, task);
        assertTrue(isCross, errorMessage);
        //Проверяем, что task не считается конфликтующей самой с собой
        errorMessage = "9. Ошибочно объявлено, объект конфликтует по времени с самим собой";
        isCross = manager.isTasksNotCrossByTime(subTask, subTask);
        assertTrue(isCross, errorMessage);
    }

    @Test
    public void shouldCorrectBuildSortedTasks() {
        //Перед началом выполнения теста сгенерировано 10 task, 6 epic,
        //Для трех task назначено стартовое время и продолжительность.
        //В данный момент к PrioritizedTasks отнесены все subTask и три task. Для начала сверим количество
        String errorMessage = "1. Размер множества sortedTasks не соответствует ожидаемому";
        boolean isValid = manager.sortedTasks.size() == (manager.getSubTaskCounter() + 3);
        assertTrue(isValid, errorMessage);
        //Теперь проверим, что sortedTask содержит все три task с установленным startTime
        errorMessage = "2. Task с установленным startTime не попали во множество sortedTasks";
        isValid = manager.sortedTasks.contains(manager.tasksList.get(1))
                && manager.sortedTasks.contains(manager.tasksList.get(3))
                && manager.sortedTasks.contains(manager.tasksList.get(5));
        assertTrue(isValid, errorMessage);
        //... и все subTask тоже попали в sortedTasks
        errorMessage = "3. Часть subTask не попала во множество sortedTasks";
        isValid = manager.subTasksList.keySet().stream()
                .map(i -> manager.subTasksList.get(i))
                .filter(subTask -> manager.sortedTasks.contains(subTask))
                .count() == manager.subTasksList.size();
        assertTrue(isValid, errorMessage);
        //Удаляем task №3. При этом будут удалены все связанные с ней подзадачи.
        Task task4Deleting = manager.getTaskWithId(3);
        manager.removeTaskWithId(3);
        errorMessage = "4. Удаленная task осталась во множестве sortedTasks";
        isValid = manager.sortedTasks.contains(task4Deleting);
        assertFalse(isValid, errorMessage);
        //Проверка для subTask
        errorMessage = "5. Во множестве sortedTasks остались subTask, связанные с удаленной task";
        isValid = manager.sortedTasks.stream()
                .filter(task -> task.getCode() == 3)
                .count() == 0;
        assertTrue(isValid, errorMessage);
    }
}