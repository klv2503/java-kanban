package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected static int taskCounter = 0;
    protected static int subTaskCounter = 0;
    protected static int epicCounter = 0;
    static Map<Integer, Task> tasksList = new HashMap<>();
    static Map<Integer, SubTask> subTasksList = new HashMap<>();
    static Map<Integer, Epic> epicsList = new HashMap<>();
    static InMemoryHistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();
    // переменные для контроля истории просмотров
    int getTaskCall = 0;
    int getSubTaskCall = 0;
    int getEpicCall = 0;
    //стандартное время выполнения одной подзадачи
    static final int standartDuration = 15;
    //создаем TreeSet, в котором отсортируем подзадачи
    Set<Task> sortedTasks = new TreeSet<>(
            (Task d1, Task d2) ->
                    (d1.getStartTime().isBefore(d2.getStartTime())) ? -1 :
                            (d1.getStartTime().isAfter(d2.getStartTime())) ? 1 : 0
    );

    public static int getTaskCounter() {
        return taskCounter;
    }

    public static int getEpicCounter() {
        return epicCounter;
    }

    public static int getSubTaskCounter() {
        return subTaskCounter;
    }

    @Override
    public InMemoryHistoryManager getInMemoryHistoryManager() {
        return inMemoryHistoryManager;
    }

    @Override
    public List<Task> getTasksList() {
        return tasksList.keySet().stream()
                .map(id -> tasksList.get(id))
                .toList();
    }

    @Override
    public List<SubTask> getSubTasksList() {
        return subTasksList.keySet().stream()
                .map(id -> subTasksList.get(id))
                .toList();
    }

    @Override
    public List<Epic> getEpicsList() {
        return epicsList.keySet().stream()
                .map(id -> epicsList.get(id))
                .toList();
    }

    @Override
    public SubTask getSubTaskWithId(int id) {
        if (subTasksList.containsKey(id)) {
            getTaskCall++;
            SubTask subTask = subTasksList.get(id);
            String hisId = "s" + id;
            inMemoryHistoryManager.add(hisId, subTask);
            return subTask;
        } else {
            return null;
        }
    }

    public void makeTask(String name, String description) {
        //Метод для генерации тестового списка задач
        String newName = name + " " + (taskCounter + 1);
        String descript = description + " " + (taskCounter + 1);
        createTask(newName, descript);
    }

    // Группа методов работы с задачами
    @Override
    public Task createTask(String name, String description) {
        Task genTask = new Task(++taskCounter, name, description);
        tasksList.put(genTask.code, genTask);
        return genTask;
    }

    @Override
    public Task getTaskWithId(int id) {
        if (tasksList.containsKey(id)) {
            getTaskCall++;
            Task task = tasksList.get(id);
            String hisId = "t" + id;
            inMemoryHistoryManager.add(hisId, task);
            return task;
        } else {
            return null;
        }
    }

    @Override
    public void renewTask(Task task) {
        if (tasksList.containsKey(task.code)) {
            tasksList.put(task.code, task);
            subTasksList.keySet().stream()
                    .map(i -> subTasksList.get(i))
                    .filter(subTask -> subTask.getCode() == task.code)
                    .forEach(subTask -> {
                        subTask.setName(task.name);
                        subTask.setDescription(task.description);
                        subTasksList.put(subTask.ownCode, subTask);
                    });
            epicsList.keySet().stream()
                    .map(i -> epicsList.get(i))
                    .forEach(epic -> epic.changeStringDataOfSubTask(task));
        }
    }

    @Override
    public void printTaskList() {
        if (tasksList.isEmpty()) {
            System.out.println("The tasklist now is empty");
        } else {
            tasksList.keySet().stream()
                    .map(i -> tasksList.get(i))
                    .forEach(System.out::println);
        }
    }

    public void printSubTaskList() {
        if (subTasksList.isEmpty()) {
            System.out.println("The SubTaskslist now is empty");
        } else {
            subTasksList.keySet().stream()
                    .map(i -> subTasksList.get(i))
                    .forEach(System.out::println);
        }
    }

    @Override
    public boolean removeTaskWithId(int taskId) {
        //Вместе с задачей удаляются созданные на ее основе подзадачи, их использование в эпиках
        // сама задача и удаленные подзадачи удаляются также из истории просмотров
        boolean result = false;
        if (tasksList.containsKey(taskId)) {
            String hisCode;
            subTasksList = subTasksList.keySet().stream()
                    .map(i -> subTasksList.get(i))
                    .peek(subTask -> {
                        if (subTask.code == taskId) {
                            inMemoryHistoryManager.remove("s" + subTask.ownCode);
                            sortedTasks.remove(subTask);
                        }
                    })
                    .filter(subTask -> !(subTask.code == taskId))
                    .collect(Collectors.toMap(SubTask::getOwnCode, subTask -> subTask));
            epicsList.keySet().stream()
                    .map(i -> epicsList.get(i))
                    .forEach(epic -> epic.deleteAllEpicsSubTasksByTaskId(taskId));
            hisCode = "t" + taskId;
            inMemoryHistoryManager.remove(hisCode);
            if (tasksList.get(taskId).getStartTime() != null)
                sortedTasks.remove(tasksList.get(taskId));
            tasksList.remove(taskId);
            result = true;
        }
        return result;
    }

    @Override
    public boolean removeSubTaskWithId(int id) {
        boolean result = false;
        if (subTasksList.containsKey(id)) {
            SubTask subTask = subTasksList.get(id);
            int epicId = getSubTasksEpic(subTask);
            String hisId = "s" + id;
            inMemoryHistoryManager.remove(hisId);
            sortedTasks.remove(subTask);
            epicsList.get(epicId).deleteEpicsSubTask(subTask);
            subTasksList.remove(id);
            result = true;
        }
        return result;
    }

    public boolean isTasksNotCrossByTime(Task task1, Task task2) {
        //Метод возвращает true, если tasks не накладываются во времени.
        //Если задачи совпадают, это не считается наложением во времени
        return task1.equals(task2)
                || task1.getEndTime().isBefore(task2.getStartTime())
                || task1.getEndTime().equals(task2.getStartTime())
                || task1.getStartTime().isAfter(task2.getEndTime())
                || task1.getStartTime().equals(task2.getEndTime());
    }

    @Override
    public boolean isValidTime(LocalDateTime startTime, Duration duration) {
        getPrioritizedTasks();
        if (startTime == null || duration.toMinutes() <= 0)
            return false;
        if (sortedTasks.isEmpty())
            return true;
        else {
            return sortedTasks.stream()
                    .allMatch(subTask -> (startTime.plus(duration).isBefore(subTask.getStartTime()))
                            || (startTime.plus(duration).equals(subTask.getStartTime()))
                            || (startTime.isAfter(subTask.getEndTime()))
                            || (startTime.equals(subTask.getEndTime()))
                    );
        }
    }

    @Override
    public boolean makeTaskExecutable(Task task, LocalDateTime lockDT, long duration) {
        //Поскольку Task не обязана иметь собственные startTime и duration (т.е. не выполняться вне эпиков)
        //метод устанавливает startTime и duration для Task, которая как бы эпик из одной задачи
        if (isValidTime(lockDT, Duration.ofMinutes(duration))) {
            task.setStartTime(lockDT);
            task.setDuration(Duration.ofMinutes(duration));
            sortedTasks.add(task);
            return true;
        } else
            return false;
    }

    @Override
    public void deleteAllTasks() {
        tasksList.clear();
        taskCounter = 0;
        subTasksList.clear();
        subTaskCounter = 0;
        epicsList.clear();
        epicCounter = 0;
        inMemoryHistoryManager.removeAllHistory();
        sortedTasks.clear();
    }

// Конец группы методов работы с задачами

    public void makeTestEpic(String name, String descript, int subTasksNumber) {
        // Метод для генерации тестового списка эпиков
        String newName = name + " " + (epicCounter + 1);
        String newDescript = descript + " " + (epicCounter + 1);
        createEpic(newName, newDescript, subTasksNumber);
    }

    @Override
    public Epic createEpic(String name, String descript, int subTasksNumber) {
        Epic epic = new Epic(++epicCounter, name, descript, Status.NEW);
        epic.setEpicsTasks(createEpicSubTasks(subTasksNumber));
        epic.setStartTime();
        epic.setDuration();
        epic.setEndTime();
        epicsList.put(epic.code, epic);
        return epic;
    }

    @Override
    public SubTask createSubTaskForEpic(int taskId, int epicId, LocalDateTime startTime, int duration) {
        Task task = tasksList.get(taskId);
        SubTask subTask = new SubTask(task, ++subTaskCounter, Status.NEW, duration, startTime);
        subTasksList.put(subTask.getOwnCode(), subTask);
        epicsList.get(epicId).getEpicsTasks().add(subTask);
        epicsList.get(epicId).countEpicStatus();
        epicsList.get(epicId).setStartTime();
        epicsList.get(epicId).setDuration();
        epicsList.get(epicId).setEndTime();
        sortedTasks.add(subTask);
        return subTask;
    }

    public SubTask makeSubTask(Task task, int duration) {
        long startShift = TimeManager.findStartShift(duration);
        LocalDateTime locDT = TimeManager.standartStart.plus(Duration.ofMinutes(startShift));
        if (isValidTime(locDT, Duration.ofMinutes(duration))) {
            SubTask newSub = new SubTask(task, ++subTaskCounter, Status.NEW, duration, locDT);
            subTasksList.put(newSub.ownCode, newSub);
            sortedTasks.add(newSub);
            return newSub;
        } else
            return null;
    }

    public ArrayList<SubTask> createEpicSubTasks(Integer howTasksInEpic) {
        ArrayList<SubTask> currentList = new ArrayList<>();
        if (howTasksInEpic > 0) {
            Random rnd = new Random();
            for (int i = 0; i < howTasksInEpic; i++) {
                int taskNumber = rnd.nextInt(taskCounter - 1) + 1;
                if (tasksList.containsKey(taskNumber)) {
                    Task task = tasksList.get(taskNumber);
                    SubTask newSub = makeSubTask(task, standartDuration);
                    if (newSub != null)
                        currentList.add(newSub);
                }
            }
        }
        return currentList;
    }

    @Override
    public void printEpicList() {
        if (epicsList.isEmpty()) {
            System.out.println("Список эпиков в данный момент пуст.");
            return;
        }
        epicsList.keySet().forEach(this::printEpicByCode);
    }

    @Override
    public void printEpicByCode(Integer epicId) {
        if (epicsList.containsKey(epicId)) {
            System.out.println(epicsList.get(epicId));
            printEpicsTasks(epicId);
        } else {
            System.out.println("Эпик с кодом " + epicId + " не существует.");
        }
    }

    public int seekSubTaskInEpic(int epicId, int subTaskId) {
        //Если подзадача не выполняется в рамках эпика, то результат -1
        int resoult = -1;
        if (epicsList.containsKey(epicId)) {
            resoult = epicsList.get(epicId).seekFirstSubTaskByID(subTaskId);
        }
        return resoult;
    }

    @Override
    public void printEpicsTasks(Integer epicId) {
        if (epicsList.containsKey(epicId)) {
            Epic epic = epicsList.get(epicId);
            ArrayList<SubTask> currentList = epic.getEpicsTasks();
            if (!currentList.isEmpty()) {
                for (int i = 0; i < currentList.size(); i++) {
                    System.out.println(currentList.get(i));
                }
            } else {
                System.out.println("Нет подзадач, связанных с эпиком " + epicId + " name: " + epic.name);
            }
        } else {
            System.out.println("Эпик с кодом " + epicId + " не существует.");
        }
    }

    @Override
    public Epic getEpicByCode(Integer epicId) {
        Epic epic = null;
        if (epicsList.containsKey(epicId)) {
            getEpicCall++;
            epic = epicsList.get(epicId);
            String hisId = "e" + epicId;
            inMemoryHistoryManager.add(hisId, epic);
        }
        return epic;
    }

    @Override
    public void changeEpic(Epic epic) {
        epicsList.put(epic.getCode(), epic);
        String hisId = "e" + epic.getCode();
        inMemoryHistoryManager.add(hisId, epic);
    }

    @Override
    public int getHowSubTasks(Integer epicId) {
        if (epicsList.containsKey(epicId)) {
            return epicsList.get(epicId).getEpicsTasks().size();
        } else {
            return 0;
        }
    }

    @Override
    public void changeSubTaskStatus(Integer epicId, Integer subNumber, Status newStat) {
        if (epicsList.containsKey(epicId)) {
            Epic epic = epicsList.get(epicId);
            if (epic.changeSubTaskStatusByIndex(subNumber, newStat)) {
                recountEpicStatus(epicId);
            }
        }
    }

    @Override
    public void recountEpicStatus(Integer epicId) {
        if (!epicsList.containsKey(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId);
            return;
        }
        epicsList.get(epicId).countEpicStatus();
    }

    @Override
    public boolean deleteEpicsSubTask(Integer epicId, Integer stId) {
        //исходный вариант метода себя изжил, с введением временных характеристик порядок подзадач в эпике
        //ничего не определяет. метод переделан под текущие потребности
        boolean result = false;
        if (!epicsList.containsKey(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId + " . Удаление его подзадач невозможно");
            return result;
        }
        SubTask subTask = subTasksList.get(stId);
        Epic epic = epicsList.get(epicId);
        result = epic.getEpicsTasks().remove(subTask);
        if (result) {
            String hisCode = "s" + subTask.getOwnCode();
            inMemoryHistoryManager.remove(hisCode);
            sortedTasks.remove(subTask);
            subTasksList.remove(subTask.getOwnCode());
            epic.recountEpicData();
        }
        return result;
    }

    @Override
    public void deleteAllEpicsSubTask(Integer epicId) {
        if (!epicsList.containsKey(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId + " . Удаление его подзадач невозможно");
            return;
        }
        ArrayList<SubTask> currentArray = epicsList.get(epicId).epicsTasks;
        if (!currentArray.isEmpty()) {
            for (int i = 0; i < currentArray.size(); i++) {
                String hisCode = "s" + currentArray.get(i).ownCode;
                inMemoryHistoryManager.remove(hisCode);
                sortedTasks.remove(currentArray.get(i));
                subTasksList.remove(currentArray.get(i).ownCode);
            }
            epicsList.get(epicId).clearEpicsSubTasks();
        }
    }

    @Override
    public SubTask getEpicsSubTaskByIndex(Integer epicCode, Integer index) {
        SubTask currentSubTask = null;
        if (index < 0) {
            return currentSubTask;
        }
        if (epicsList.containsKey(epicCode)) {
            currentSubTask = epicsList.get(epicCode).getSubTaskByIndex(index);
        }
        if (!(currentSubTask == null)) {
            String hisId = "s" + currentSubTask.ownCode;
            inMemoryHistoryManager.add(hisId, currentSubTask);
            getSubTaskCall++;
        }
        return currentSubTask;
    }

    @Override
    public void addNewSubToEpic(Integer epicId, SubTask subTask) {
        //после появления характеристик времени порядок подзадач в эпике должен регулироваться ими, а не местом
        //поэтому метод упрощен
        if (!epicsList.containsKey(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId);
            return;
        }
        epicsList.get(epicId).addSubTaskInEpic(subTask);
        sortedTasks.add(subTask);
    }

    @Override
    public boolean deleteEpic(Integer epicId) {
        boolean result = false;
        if (epicsList.containsKey(epicId)) {
            deleteAllEpicsSubTask(epicId);
            String hisId = "e" + epicId;
            inMemoryHistoryManager.remove(hisId);
            epicsList.remove(epicId);
            result = true;
        }
        return result;
    }

    @Override
    public void deleteAllSubTasks() {
        //Перед очисткой списка подзадач убираем все подзадачи из всех эпиков
        epicsList.keySet().stream().forEach(i -> deleteAllEpicsSubTask(i));
        subTasksList.clear();
    }

    @Override
    public void deleteAllEpics() {
        //Подзадачи без эпиков не имеют смысла, поэтому их тоже удаляем
        if (!epicsList.isEmpty()) {
            deleteAllSubTasks();
            epicsList.keySet().stream().forEach(i -> inMemoryHistoryManager.remove("e" + i));
            epicsList.clear();
        }
    }

    //2 метода добавлены для доступа из HttpClient
    @Override
    public boolean removeSorted(Task task) {
        return sortedTasks.remove(task);
    }

    @Override
    public void addSorted(Task task) {
        sortedTasks.add(task);
    }

    @Override
    public int getSubTasksEpic(SubTask subTask) {
        Optional<Integer> epicId = epicsList.keySet().stream()
                .map(id -> epicsList.get(id))
                .filter(epic -> epic.getEpicsTasks().contains(subTask))
                .map(epic -> epic.getCode())
                .findFirst();
        //если эпик не найден, возвращаем ноль (id эпика всегда целое положительное число)
        return epicId.orElse(0);
    }

    public void createSortedTasks() {
        sortedTasks.clear();
        tasksList.keySet().stream()
                .map(i -> tasksList.get(i))
                .filter(task -> task.startTime != null)
                .forEach(task -> sortedTasks.add(task));
        subTasksList.keySet().stream()
                .map(i -> subTasksList.get(i))
                .filter(subTask -> subTask.startTime != null)
                .forEach(subTask -> sortedTasks.add(subTask));
    }

    public List<Task> getPrioritizedTasks() {
        if (sortedTasks.isEmpty())
            createSortedTasks();
        return sortedTasks.stream().toList();
    }

    public void printHistory() {
        ArrayList<Task> currentList = inMemoryHistoryManager.getHistory();
        if (!currentList.isEmpty()) {
            for (int i = 0; i < currentList.size(); i++) {
                System.out.println(currentList.get(i));
            }
        } else {
            System.out.println("История просмотров пока пуста.");
        }
    }

    public int getSpecificEpic() { //ищем эпик, у которого больше двух подзадач, берем первый попавшийся
        Optional<Integer> result = epicsList.keySet().stream()
                .filter(epicId -> 2 < getHowSubTasks(epicId))
                .findFirst();
        return (result.isEmpty()) ? -1 : result.get();
    }
}