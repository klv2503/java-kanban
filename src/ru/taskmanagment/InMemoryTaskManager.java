package ru.taskmanagment;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private static int taskCounter = 0;
    private static int epicCounter = 0;
    static Map<Integer, Task> tasksList = new HashMap<>();
    static Map<Integer, SubTask> subTasksList = new HashMap<>();
    static Map<Integer, Epic> epicsList = new HashMap<>();
    static InMemoryHistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();

    // переменные для контроля истории просмотров
    int getTaskCall = 0;
    int getSubTaskCall = 0;
    int getEpicCall = 0;

    public static int getTaskCounter() {
        return taskCounter;
    }

    public static int getEpicCounter() {
        return epicCounter;
    }

    public void makeTask(String name, String description) {//Метод для генерации тестового списка задач
        String newName = name + " " + (taskCounter + 1);
        String descript = description + " " + (taskCounter + 1);
        createTask(newName, descript);
    }

    // Группа методов работы с задачами
    @Override
    public boolean isTaskExist(Integer code) {
        return tasksList.containsKey(code);
    }

    @Override
    public void createTask(String name, String description) {
        Task genTask = new Task(++taskCounter, name, description);
        tasksList.put(genTask.code, genTask);
        SubTask subTask = new SubTask(genTask, Status.NEW);
        subTasksList.put(subTask.code, subTask);
    }

    @Override
    public Task getTaskWithId(int id) {
        if (tasksList.containsKey(id)) {
            getTaskCall++;
            Task task = tasksList.get(id);
            inMemoryHistoryManager.add(task);
            return task;
        } else {
            return null;
        }
    }

    @Override
    public void renewTask(Task task) {
        if (isTaskExist(task.code)) {
            tasksList.put(task.code, task);
            SubTask subTask = new SubTask(task, Status.NEW);
            subTasksList.put(subTask.code, subTask);
            for (int epicId : epicsList.keySet()) {
                epicsList.get(epicId).changeStringDataOfSubTask(subTask);
            }
        }
    }

    @Override
    public void printTaskList() {
        if (tasksList.isEmpty()) {
            System.out.println("The tasklist now is empty");
        } else {
            for (int i : tasksList.keySet()) {
                System.out.println(tasksList.get(i));
            }
        }
    }

    public void printSubTaskList() {
        if (subTasksList.isEmpty()) {
            System.out.println("The SubTaskslist now is empty");
        } else {
            for (int i : subTasksList.keySet()) {
                System.out.println(subTasksList.get(i));
            }
        }
    }

    @Override
    public void removeTaskWithId(int taskId) {
        //Вместе с задачей удаляется созданная на ее основе подзадача и использование этой подзадачи в эпиках
        if (isTaskExist(taskId)) {
            for (int epicId : epicsList.keySet()) {
                epicsList.get(epicId).deleteAllEpicsSubTasksById(taskId);
            }
        }
        subTasksList.remove(taskId);
        tasksList.remove(taskId);
    }

    @Override
    public void deleteAllTasks() {
        tasksList.clear();
        subTasksList.clear();
    }

    @Override
    public boolean isSubTaskExist(Integer code) {
        return subTasksList.containsKey(code);
    }

// Конец группы методов работы с задачами

    public void makeTestEpic(String name, String descript, int subTasksNumber) {
        // Метод для генерации тестового списка эпиков
        String newName = name + " " + (epicCounter + 1);
        String newDescript = descript + " " + (epicCounter + 1);
        createEpic(newName, newDescript, subTasksNumber);
    }

    @Override
    public void createEpic(String name, String descript, int subTasksNumber) {
        Epic epic = new Epic(++epicCounter, name, descript, Status.NEW);
        epic.setEpicsTasks(createEpicSubTasks(subTasksNumber));
        epicsList.put(epic.code, epic);
    }

    public ArrayList<SubTask> createEpicSubTasks(Integer howTasksInEpic) {
        ArrayList<SubTask> currentList = new ArrayList<>();
        if (howTasksInEpic > 0) {
            Random rnd = new Random();
            for (int i = 0; i < howTasksInEpic; i++) {
                int taskNumber = rnd.nextInt(taskCounter - 1) + 1;
                if (subTasksList.containsKey(taskNumber)) {
                    SubTask oldSub = subTasksList.get(taskNumber);
                    SubTask newSub = new SubTask(oldSub, Status.NEW);
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
        for (Integer code : epicsList.keySet()) {
            printEpicByCode(code);
        }
    }

    @Override
    public void printEpicByCode(Integer epicId) {
        if (isEpicExist(epicId)) {
            System.out.println(epicsList.get(epicId));
            printEpicsTasks(epicId);
        } else {
            System.out.println("Эпик с кодом " + epicId + " не существует.");
        }
    }

    public int seekSubTaskInEpic(int epicId, int subTaskId) {
        //Если подзадача не выполняется в рамках эпика, то результат -1
        int resoult = -1;
        if (isEpicExist(epicId)) {
            resoult = epicsList.get(epicId).seekFirstSubTaskByID(subTaskId);
        }
        return resoult;
    }

    @Override
    public void printEpicsTasks(Integer epicId) {
        if (isEpicExist(epicId)) {
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
    public boolean isEpicExist(Integer epicId) {
        return epicsList.containsKey(epicId);
    }

    @Override
    public Epic getEpicByCode(Integer epicId) {
        Epic epic = null;
        if (epicsList.containsKey(epicId)) {
            getEpicCall++;
            epic = epicsList.get(epicId);
            inMemoryHistoryManager.add(epic);
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

    public static void changeSubTaskStatus(Integer epicId, Integer subNumber, Status newStat) {
        if (epicsList.containsKey(epicId)) {
            epicsList.get(epicId).changeSubTaskStatusByIndex(subNumber, newStat);
            }
    }

    @Override
    public void countEpicStatus(Integer epicId) {
        if (!isEpicExist(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId);
            return;
        }
        epicsList.get(epicId).countEpicStatus();
    }

    @Override
    public boolean deleteEpicsSubTask(Integer epicId, Integer index) {
        boolean resoult = false;
        if (isEpicExist(epicId)) {
            resoult = epicsList.get(epicId).deleteEpicsSubTaskByIndex(index);
        }
        return resoult;
    }

    @Override
    public void deleteAllEpicsSubTask(Integer epicId) {
        if (isEpicExist(epicId)) {
            epicsList.get(epicId).deleteAllEpicsSubTasks();
        } else {
            System.out.println("Нет эпика с кодом " + epicId + " . Удаление его подзадач невозможно");
        }
    }

    @Override
    public SubTask getEpicsSubTaskByIndex(Integer epicCode, Integer index) {
        SubTask currentSubTask = null;
        if (index < 0) {
            return currentSubTask;
        }
        if (isEpicExist(epicCode)) {
            currentSubTask = epicsList.get(epicCode).getSubTaskByIndex(index);
        }
        if (!(currentSubTask == null)) {
            inMemoryHistoryManager.add(currentSubTask);
            getSubTaskCall++;
        }
        return currentSubTask;
    }

    @Override
    public void addNewSubToEpic(Integer epicId, Integer subTaskId, Integer place) {
        if (!isEpicExist(epicId)) {
            System.out.println("Нет эпика с кодом " + epicId);
            return;
        }
        if (!isSubTaskExist(subTaskId)) {
            System.out.println("Нет задачи с кодом " + subTaskId);
            return;
        }
        epicsList.get(epicId).addSubTaskInEpic(subTasksList.get(subTaskId),place);
    }

    @Override
    public void deleteEpic(Integer epicId) {
        if (isEpicExist(epicId)) {
            epicsList.remove(epicId);
        } else {
            System.out.println("Нет эпика с кодом " + epicId + " . Его удаление невозможно.");
        }
    }

    @Override
    public void deleteAllSubTasks() {
        //Перед очисткой списка подзадач убираем все подзадачи из всех эпиков
        if (!epicsList.isEmpty()) {
            for (int code : epicsList.keySet()) {
                epicsList.get(code).deleteAllEpicsSubTasks();
            }
        }
        subTasksList.clear();
    }

    @Override
    public void deleteAllEpics() {
        if (!epicsList.isEmpty()) {
            epicsList.clear();
        }
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
        for (int epicId : epicsList.keySet()) {
            if (2 < getHowSubTasks(epicId)) {
                return epicId;
            }
        }
        return -1;
    }

}
