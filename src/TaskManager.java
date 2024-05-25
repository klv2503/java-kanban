import java.util.*;

public class TaskManager {

    private static int taskCounter = 0;
    private static int epicCounter = 0;
    static Map<Integer, Task> tasksList = new HashMap<>();
    static Map<Integer, Epic> epicList = new HashMap<>();
    static Map<String, ArrayList<SubTask>> subTasksList = new HashMap<>();

//генерация тестового списка задач

    public static Task makeTask(Task task) {
        taskCounter++;
        String name = task.name + " " + taskCounter;
        String descript = task.description + " " + taskCounter;
        Task genTask = new Task(taskCounter, name, descript);
        tasksList.put(genTask.code, genTask);
        return genTask;
    }

    public static boolean isTaskExist(Integer code) {
        return tasksList.containsKey(code);
    }

    public static Task createTask(Task task) {
        if (task == null) {
            return null;
        } else {
            Task genTask = new Task(++taskCounter, task.name, task.description);
            tasksList.put(genTask.code, genTask);
            return genTask;
        }
    }

    public static void printTaskList() {
        if (tasksList.isEmpty()) {
            System.out.println("The tasklist now is empty");
        } else {
            for (int i : tasksList.keySet()) {
                System.out.println(tasksList.get(i));
            }
        }
    }

    public static Task getTaskWithId(int id) {
        if (tasksList.containsKey(id)) {
            return tasksList.get(id);
        } else return null;
    }

    public static void renewTask(Task task) {
        Task currentTask = null;
        if (tasksList.containsKey(task.code)) {
            currentTask = tasksList.get(task.code);
            currentTask.name = task.name;
            currentTask.description = task.description;
            tasksList.put(currentTask.code, currentTask);
        }
    }

    public static void removeTaskWithId(int id) {
        if (tasksList.containsKey(id)) {
            tasksList.remove(id);
        }
    }

    public static void deleteAllTasks() {
        tasksList.clear();
    }

    // Генерация тестового списка эпиков
    public static void makeTestEpic(String name) {
        Epic epic = new Epic(name, Status.NEW);
        epic.setEpicName(epic.epicName);
        createEpic(epic);
    }

    public static void createEpic(Epic epic) {
        epic.setEpicCode(++epicCounter);
        epic.setEpicName(epic.epicName + " " + epicCounter);
        epicList.put(epic.epicCode, epic);
        ArrayList<SubTask> currentList = generateEpicTasks();
        //  if (currentList.isEmpty()) System.out.println("пустой список задача " + epic.epicName);
        subTasksList.put(epic.epicName, currentList);
    }

    public static ArrayList<SubTask> generateEpicTasks() {
        ArrayList<SubTask> currentList = new ArrayList<>();
        Random rnd = new Random();
        int howTasksInEpic = rnd.nextInt(tasksList.size() - 1) + 1;
        for (int i = 0; i < howTasksInEpic; i++) {
            int taskNumber = rnd.nextInt(taskCounter - 1) + 1;
            if (tasksList.containsKey(taskNumber)) {
                Task task = tasksList.get(taskNumber);
                SubTask newSub = new SubTask(task.code, task.name,
                        task.description, (i + 1), Status.NEW);
                currentList.add(newSub);
            }
        }
        return currentList;
    }

    public static void printEpicList() {
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

    public static void printEpicByCode(Integer code) {
        if (!epicList.containsKey(code)) {
            System.out.println("Нет эпика с кодом " + code);
            return;
        }
        System.out.println(epicList.get(code));
        printEpicsTasks(code);
    }

    public static void printEpicsTasks(Integer code) {
        if (!epicList.containsKey(code)) {
            System.out.println("Эпика с кодом " + code + " в данный момент нет.");
        }
        if (subTasksList.isEmpty()) {
            System.out.println("Нет подзадач, связанных с эпиком " + code);
            return;
        }
        Epic epic = epicList.get(code);
        ArrayList<SubTask> workArray = subTasksList.get(epic.epicName);
        if (workArray == null) {
            System.out.println("Нет подзадач, связанных с эпиком " + code);
            return;
        }
        if (workArray.isEmpty()) {
            System.out.println("Нет подзадач, связанных с эпиком " + code + " name: " + epic.epicName);
            return;
        }
        for (int i = 0; i < workArray.size(); i++) {
            System.out.println(workArray.get(i));
        }
    }

    public static boolean isEpicExist(Integer code) {
        return epicList.containsKey(code);
    }

    public static Epic getEpicByCode(Integer code) {
        Epic epic = null;
        if (epicList.containsKey(code)) epic = epicList.get(code);
        return epic;
    }

    public static int getHowSubTasks(Integer code) {
        Epic epic = getEpicByCode(code);
        if (subTasksList.containsKey(epic.epicName)) {
            return subTasksList.get(epic.epicName).size();
        } else return 0;
    }

    public static int getTaskCounter() {
        return taskCounter;
    }

    public static int getEpicCounter() {
        return epicCounter;
    }

    public static void changeSubTaskStatus(String name, Integer subNumber, Status newStat) {
        if (subTasksList.containsKey(name)) {
            ArrayList<SubTask> currentList = new ArrayList<>();
            currentList = subTasksList.get(name);
            if (currentList.size() > (subNumber - 1)) {
                SubTask sTsk = new SubTask(0, "", "", 0, Status.NEW);
                sTsk = currentList.get(subNumber - 1);
                sTsk.setStatus(newStat);
                currentList.remove(subNumber - 1);
                currentList.add(subNumber - 1, sTsk);
            }
        }
    }

    public static void countEpicStatus(Integer id) {
        if (!epicList.containsKey(id)) {
            System.out.println("Нет такого эпика");
            return;
        }
        Status stat = Status.NEW;
        Epic epic = epicList.get(id);
        String name = epic.epicName;
        ArrayList<SubTask> currentList = subTasksList.get(name);
        if (currentList.isEmpty()) {
            epic.setEpicStatus(Status.NEW);
            return;
        }
        int statNew = 0;
        int statProgress = 0;
        int statDone = 0;
        for (int i = 0; i < currentList.size(); i++) {
            SubTask currentSub = currentList.get(i);
            switch (currentSub.status) {
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
                    System.out.println("Эпик " + id + " Подзадача " + currentSub.codeInEpic + " Статус не установлен.");
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

    public static void clearEpicsSub(Integer id) {
        if (!epicList.containsKey(id)) {
            System.out.println("Нет такого эпика.");
            return;
        }
        Epic epic = epicList.get(id);
        String name = epic.epicName;
        ArrayList<SubTask> currentList = subTasksList.get(name);
        currentList.clear();
        subTasksList.put(name, currentList);
        epic.setEpicStatus(Status.NEW);
        epicList.put(id, epic);
    }

    public static void addNewSubToEpic(Integer epicId, Integer taskId) {
        if (!epicList.containsKey(epicId)) {
            System.out.println("Нет такого эпика.");
            return;
        }
        if (!tasksList.containsKey(taskId)) {
            System.out.println("Нет такой задачи.");
            return;
        }
        Epic epic = epicList.get(epicId);
        Task task = tasksList.get(taskId);
        String name = epic.epicName;
        ArrayList<SubTask> currentList = subTasksList.get(name);
        int codeSubInEpic = 0;
        if (currentList.isEmpty()) {
            codeSubInEpic = 1;
        } else {
            codeSubInEpic = currentList.size() + 1;
        }
        SubTask newSub = new SubTask(taskId, task.name, task.description, codeSubInEpic, Status.NEW);
        currentList.add(newSub);
        subTasksList.put(name, currentList);
    }

    public static void deleteEpic(Integer id) {
        if (!epicList.containsKey(id)) {
            System.out.println("Нет такого эпика.");
            return;
        }
        Epic epic = epicList.get(id);
        if (subTasksList.containsKey(epic.epicName)) subTasksList.remove(epic.epicName);
        epicList.remove(id);
    }

    public static void clearSubTasks() {
        if (!subTasksList.isEmpty()) subTasksList.clear();
    }

    public static void clearEpics() {
        if (!epicList.isEmpty()) epicList.clear();
    }

    public static int getSpecificEpic() { //ищем эпик, у которого больше двух подзадач, берем первый попавшийся
        for (int i = 0; i < epicCounter; i++) {
            if (getHowSubTasks(i + 1) > 2) {
                return i + 1;
            }
        }
        return 0;
    }
}
