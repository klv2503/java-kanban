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
    //создаем TreeSet, в котором отсортируем подзадачи
    static final int standartDuration = 15; //стандартное время выполнения одной подзадачи
    Set<SubTask> sortedSubTasks = new TreeSet<>(
            (SubTask d1, SubTask d2) ->
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

    public void makeTask(String name, String description) {
        //Метод для генерации тестового списка задач
        String newName = name + " " + (taskCounter + 1);
        String descript = description + " " + (taskCounter + 1);
        createTask(newName, descript);
    }

    // Группа методов работы с задачами
    @Override
    public void createTask(String name, String description) {
        Task genTask = new Task(++taskCounter, name, description);
        tasksList.put(genTask.code, genTask);
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
    public void removeTaskWithId(int taskId) {
        //Вместе с задачей удаляются созданные на ее основе подзадачи, их использование в эпиках
        // сама задача и удаленные подзадачи удаляются также из истории просмотров
        if (tasksList.containsKey(taskId)) {
            String hisCode;
            subTasksList = subTasksList.keySet().stream()
                    .map(i -> subTasksList.get(i))
                    .peek(subTask -> {
                        if (subTask.code == taskId)
                            inMemoryHistoryManager.remove("s" + subTask.ownCode);
                    })
                    .filter(subTask -> !(subTask.code == taskId))
                    .collect(Collectors.toMap(SubTask::getOwnCode, subTask -> subTask));
            epicsList.keySet().stream()
                    .map(i -> epicsList.get(i))
                    .forEach(epic -> epic.deleteAllEpicsSubTasksByTaskId(taskId));
            hisCode = "t" + taskId;
            inMemoryHistoryManager.remove(hisCode);
            tasksList.remove(taskId);
        }
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

    public boolean isValidSubTask(SubTask testedSubTask) {
        getPrioritizedTasks();
        if (sortedSubTasks.isEmpty())
            return true;
        else
            return sortedSubTasks.stream()
                    .allMatch(subTask -> (testedSubTask.getEndTime().isBefore(subTask.getStartTime()))
                            || (testedSubTask.getEndTime().equals(subTask.getStartTime()))
                            || (testedSubTask.getStartTime().isAfter(subTask.getEndTime()))
                            || (testedSubTask.getStartTime().equals(subTask.getEndTime()))
                    );
    }

    public SubTask makeSubTask(Task task, int duration) {
        long startShift = TimeManager.findStartShift(duration);
        LocalDateTime locDT = TimeManager.standartStart.plus(Duration.ofMinutes(startShift));
        SubTask newSub = new SubTask(task, ++subTaskCounter, Status.NEW, duration, locDT);
        if (isValidSubTask(newSub)) {
            subTasksList.put(newSub.ownCode, newSub);
            sortedSubTasks.add(newSub);
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
    public boolean deleteEpicsSubTask(Integer epicId, Integer stNum) {
        //stNum - номер подзадачи согласно внутренней нумерации подзадач в данном эпике от 1
        boolean result = false;
        if (!epicsList.containsKey(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId + " . Удаление его подзадач невозможно");
            return result;
        }
        Epic epic = epicsList.get(epicId);
        if ((stNum > 0) && (stNum <= epic.epicsTasks.size())) {
            int subTaskId = epic.epicsTasks.get(stNum - 1).ownCode;
            String hisCode = "s" + subTaskId;
            inMemoryHistoryManager.remove(hisCode);
            subTasksList.remove(subTaskId);
            result = epic.deleteEpicsSubTaskByIndex(stNum - 1);
            if (result)
                epic.countEpicStatus();
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
    public void addNewSubToEpic(Integer epicId, Integer taskId, Integer place) {
        if (!epicsList.containsKey(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId);
            return;
        }
        if (!tasksList.containsKey(taskId)) {
            System.out.println("Нет задачи с кодом " + taskId);
            return;
        }
        SubTask subTask = makeSubTask(tasksList.get(taskId), standartDuration);
        if (subTask != null)
            epicsList.get(epicId).addSubTaskInEpic(subTask, place);
        else
            System.out.println("Подзадачу создать не удалось из-за конфликта по времени");
    }

    @Override
    public void deleteEpic(Integer epicId) {
        if (epicsList.containsKey(epicId)) {
            deleteAllEpicsSubTask(epicId);
            String hisId = "e" + epicId;
            inMemoryHistoryManager.remove(hisId);
            epicsList.remove(epicId);
        } else {
            System.out.println("Нет эпика с кодом " + epicId + " . Его удаление невозможно.");
        }
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

    public void getPrioritizedTasks() {
        sortedSubTasks.clear();
        subTasksList.keySet().stream()
                .map(i -> subTasksList.get(i))
                .filter(subTask -> subTask.startTime != null)
                .forEach(subTask -> sortedSubTasks.add(subTask));
    }

    public void printPrioritizedTasks() {
        if (sortedSubTasks.isEmpty())
            getPrioritizedTasks();
        sortedSubTasks.stream().forEach(System.out::println);
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