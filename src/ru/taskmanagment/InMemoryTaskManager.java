package ru.taskmanagment;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private static int taskCounter = 0;
    private static int epicCounter = 0;
    static Map<Integer, Task> tasksList = new HashMap<>();
    static Map<Integer, Epic> epicList = new HashMap<>();
    static Map<Integer, SubTask> subTasksList = new HashMap<>();
    static Map<Integer, ArrayList<SubTask>> allEpics = new HashMap<>();
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
            for (int key : epicList.keySet()) {
                ArrayList<SubTask> currentList = allEpics.get(key);
                if (!currentList.isEmpty()) {
                    for (int i = 0; i < currentList.size(); i++) {
                        SubTask subTask1 = currentList.get(i);
                        if (subTask1.code == subTask.code) {
                            subTask1.name = subTask.name;
                            subTask1.description = subTask.description;
                            currentList.set(i, subTask1);
                        }
                    }
                }
                allEpics.put(key, currentList);
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
            for (int epicId : epicList.keySet()) {
                while (seekSubTaskInEpic(epicId, taskId) >= 0) {
                    ArrayList<SubTask> currentList = allEpics.get(epicId);
                    currentList.remove(seekSubTaskInEpic(epicId, taskId));
                    allEpics.put(epicId, currentList);
                }
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

// Конец группы работы с задачами

    public void makeTestEpic(String name, String descript, int subTasksNumber) {
        // Метод для генерации тестового списка эпиков
        String newName = name + " " + (epicCounter + 1);
        String newDescript = descript + " " + (epicCounter + 1);
        createEpic(newName, newDescript, subTasksNumber);
    }

    @Override
    public void createEpic(String name, String descript, int subTasksNumber) {
        Epic epic = new Epic(++epicCounter, name, descript, Status.NEW);
        epicList.put(epic.code, epic);
        ArrayList<SubTask> currentList = new ArrayList<>();
        allEpics.put(epic.code, currentList);
        currentList = createEpicSubTasks(subTasksNumber);
        allEpics.put(epic.code, currentList);
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
        if (epicList == null) {
            System.out.println("Список эпиков в данный момент пуст.");
            return;
        }
        if (epicList.isEmpty()) {
            System.out.println("Список эпиков в данный момент пуст.");
            return;
        }
        for (Integer code : epicList.keySet()) {
            printEpicByCode(code);
        }
    }

    @Override
    public void printEpicByCode(Integer code) {
        if (isEpicExist(code)) {
            System.out.println(epicList.get(code));
            printEpicsTasks(code);
        } else {
            System.out.println("Эпик с кодом " + code + " не существует.");
        }
    }

    public int seekSubTaskInEpic(int epicId, int subTaskId) {
        int resoult = -1;
        if (isEpicExist(epicId)) {
            ArrayList<SubTask> currentList = allEpics.get(epicId);
            for (int i = 0; i < currentList.size(); i++) {
                if (subTaskId == currentList.get(i).code) {
                    resoult = i;
                    break;
                }
            }
        }
        return resoult;
    }

    @Override
    public void printEpicsTasks(Integer code) {
        if (isEpicExist(code)) {
            Epic epic = epicList.get(code);
            ArrayList<SubTask> currentList = allEpics.get(code);
            if (!currentList.isEmpty()) {
                for (int i = 0; i < currentList.size(); i++) {
                    System.out.println(currentList.get(i));
                }
            } else {
                System.out.println("Нет подзадач, связанных с эпиком " + code + " name: " + epic.name);
            }
        } else {
            System.out.println("Эпик с кодом " + code + " не существует.");
        }
    }

    @Override
    public boolean isEpicExist(Integer code) {
        return epicList.containsKey(code);
    }

    @Override
    public Epic getEpicByCode(Integer code) {
        Epic epic = null;
        if (epicList.containsKey(code)) {
            getEpicCall++;
            epic = epicList.get(code);
            inMemoryHistoryManager.add(epic);
        }
        return epic;
    }

    @Override
    public int getHowSubTasks(Integer code) {
        if (allEpics.containsKey(code)) {
            return allEpics.get(code).size();
        } else {
            return 0;
        }
    }

    public static void changeSubTaskStatus(Integer code, Integer subNumber, Status newStat) {
        if (allEpics.containsKey(code)) {
            ArrayList<SubTask> currentList = allEpics.get(code);
            if (currentList.size() > (subNumber - 1)) {
                SubTask sTsk = currentList.get(subNumber - 1);
                sTsk.setStatus(newStat);
                currentList.remove(subNumber - 1);
                currentList.add(subNumber - 1, sTsk);
            }
        }
    }

    @Override
    public void countEpicStatus(Integer id) {
        if (!isEpicExist(id)) {
            System.out.println("Нет эпика с кодом " + id);
            return;
        }
        Status stat = Status.NEW;
        Epic epic = epicList.get(id);
        ArrayList<SubTask> currentList = allEpics.get(epic.code);
        if (currentList.isEmpty()) {
            epic.setEpicStatus(Status.NEW);
            return;
        }
        int statNew = 0;
        int statProgress = 0;
        int statDone = 0;
        for (int i = 0; i < currentList.size(); i++) {
            switch (currentList.get(i).status) {
                case NEW: {
                    statNew++;
                    break;
                }
                case IN_PROGRESS: {
                    statProgress++;
                    break;
                }
                case DONE: {
                    statDone++;
                    break;
                }
                default:
                    System.out.println("Эпик " + id + " Подзадача " + (i + 1) + " Статус не установлен.");
            }
        }
        if (statProgress != 0) {
            epic.setEpicStatus(Status.IN_PROGRESS);
        } else if (statDone == 0) {
            epic.setEpicStatus(Status.NEW);
        } else {
            epic.setEpicStatus(Status.DONE);
        }
    }

    @Override
    public boolean deleteEpicsSubTask(Integer epicId, Integer subTaskNum) {
        boolean isSuccess = false;
        if (isEpicExist(epicId)) {
            ArrayList<SubTask> currentList = allEpics.get(epicId);
            if (!currentList.isEmpty() && (subTaskNum <= currentList.size())) {
                isSuccess = (!(currentList.remove(subTaskNum - 1) == null));
                if (isSuccess) {
                    allEpics.put(epicId, currentList);
                }
            }
        }
        return isSuccess;
    }

    @Override
    public void deleteAllEpicsSubTask(Integer id) {
        if (isEpicExist(id)) {
            Epic epic = epicList.get(id);
            ArrayList<SubTask> currentList = new ArrayList<>();
            allEpics.put(id, currentList);
            epic.setEpicStatus(Status.NEW);
            epicList.put(id, epic);
        } else {
            System.out.println("Нет эпика с кодом " + id + " . Удаление его подзадач невозможно");
        }
    }

    @Override
    public SubTask getEpicsSubTaskByNumber(Integer epicCode, Integer subTaskNumber) {
        SubTask currentSubTask = null;
        if (subTaskNumber <= 0) {
            return currentSubTask;
        }
        if (isEpicExist(epicCode)) {
            ArrayList<SubTask> currentList = allEpics.get(epicCode);
            if (!currentList.isEmpty() && (subTaskNumber <= currentList.size()))
                currentSubTask = currentList.get(subTaskNumber - 1);
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
        ArrayList<SubTask> currentList = allEpics.get(epicId);
        SubTask oldSub = subTasksList.get(subTaskId);
        SubTask newSub = new SubTask(oldSub, Status.NEW);
        if (!currentList.isEmpty()) {
            if (place > currentList.size()) {
                System.out.println("У эпика № " + epicId + " подзадач меньше, чем " + place + ".\n"
                        + "Добавляем подзадачу в конец списка.");
                currentList.add(newSub);
            } else {
                currentList.add(place - 1, newSub);
            }
        }
        if (currentList.isEmpty()) {
            System.out.println("Список подзадач пуст. Подзадача " + subTaskId + " добавляется под № 1.");
            currentList.add(newSub);
        }
        allEpics.put(epicId, currentList);
    }

    @Override
    public void deleteEpic(Integer id) {
        if (isEpicExist(id)) {
            Epic epic = epicList.get(id);
            //Перед удалением эпика удаляем все его подзадачи
            allEpics.remove(epic.code);
            epicList.remove(id);
        } else {
            System.out.println("Нет эпика с кодом " + id + " . Его удаление невозможно.");
        }
    }

    @Override
    public void deleteAllSubTasks() {
        if (!allEpics.isEmpty()) {
            for (int code : allEpics.keySet()) {
                ArrayList<SubTask> currentList = new ArrayList<>();
                allEpics.put(code, currentList);
            }
        }
    }

    @Override
    public void deleteAllEpics() {
        if (!epicList.isEmpty()) {
            epicList.clear();
        }
        deleteAllSubTasks(); //если нет списка эпиков, список подзадач не имеет смысла
    }

 /*   public boolean addEpicAsSubTask(Integer mainEpic, Integer dependEpic) {
        if (Objects.equals(mainEpic, dependEpic)) {
            System.out.println("Эпик не может быть своей же подзадачей. Добавление невозможно.");
            return false;
        }
        if (!isEpicExist(mainEpic)) {
            System.out.println("Эпик " + mainEpic + " не существует. Добавление подзадач в него невозможно.");
            return false;
        }
        if (!isEpicExist(dependEpic)) {
            System.out.println("Эпик " + dependEpic + " не существует. Его добавление как подзадачи невозможно.");
            return false;
        }
        ArrayList<SubTask> currentList = allEpics.get(mainEpic);
        addEpicToEpic(currentList, dependEpic);
        allEpics.put(mainEpic, currentList);
        return true;
    }

    @Override
    public void addEpicToEpic(ArrayList<SubTask> currentList, Integer dependEpic) {
        Epic epic = epicList.get(dependEpic);
        Epic newSub = new Epic(epic.code, epic.name, epic.description, Status.NEW);
        currentList.add((SubTask) epic);
    }*/

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
        for (int i = 0; i < epicCounter; i++) {
            if (2 < getHowSubTasks(i + 1)) {
                return i + 1;
            }
        }
        return 0;
    }
}
