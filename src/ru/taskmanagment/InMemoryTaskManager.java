package ru.taskmanagment;

import java.util.*;

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
            for (int i : subTasksList.keySet()) {
                if (subTasksList.get(i).code == task.code) {
                    SubTask subTask = subTasksList.get(i);
                    subTask.name = task.name;
                    subTask.description = task.description;
                    subTasksList.put(i, subTask);
                }
            }
            for (int epicId : epicsList.keySet()) {
                epicsList.get(epicId).changeStringDataOfSubTask(task);
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
        //Вместе с задачей удаляются созданные на ее основе подзадачи, их использование в эпиках
        // сама задача и удаленные подзадачи удаляются также из истории просмотров
        if (tasksList.containsKey(taskId)) {
            String hisCode;
            for (int epicId : epicsList.keySet()) {
                ArrayList<Integer> subTaskIndexes = new ArrayList<>();
                Epic epic = epicsList.get(epicId);
                subTaskIndexes = epic.seekSubTasksIndexesByTaskId(taskId);
                if (!subTaskIndexes.isEmpty()) {
                    for (int i : subTaskIndexes) {
                        int codeToErase = epic.epicsTasks.get(i).ownCode;
                        subTasksList.remove(codeToErase);
                        hisCode = "s" + codeToErase;
                        inMemoryHistoryManager.remove(hisCode);
                    }
                    epic.deleteAllEpicsSubTasksByTaskId(taskId);
                }
            }
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
    public void createEpic(String name, String descript, int subTasksNumber) {
        Epic epic = new Epic(++epicCounter, name, descript, Status.NEW);
        epic.setEpicsTasks(createEpicSubTasks(subTasksNumber));
        epicsList.put(epic.code, epic);
    }

    public SubTask makeSubTask(Task task) {
        SubTask newSub = new SubTask(task, ++subTaskCounter, Status.NEW);
        subTasksList.put(newSub.ownCode, newSub);
        return newSub;
    }

    public ArrayList<SubTask> createEpicSubTasks(Integer howTasksInEpic) {
        ArrayList<SubTask> currentList = new ArrayList<>();
        if (howTasksInEpic > 0) {
            Random rnd = new Random();
            for (int i = 0; i < howTasksInEpic; i++) {
                int taskNumber = rnd.nextInt(taskCounter - 1) + 1;
                if (tasksList.containsKey(taskNumber)) {
                    Task task = tasksList.get(taskNumber);
                    currentList.add(makeSubTask(task));
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
            result = epic.deleteEpicsSubTaskByIndex(stNum-1);
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
        SubTask subTask = new SubTask(tasksList.get(taskId), ++subTaskCounter, Status.NEW);
        subTasksList.put(subTask.ownCode, subTask);
        epicsList.get(epicId).addSubTaskInEpic(subTask, place);
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
        if (!epicsList.isEmpty()) {
            for (int code : epicsList.keySet()) {
                deleteAllEpicsSubTask(code);
            }
        }
        subTasksList.clear();
    }

    @Override
    public void deleteAllEpics() {
        //Подзадачи без эпиков не имеют смысла, поэтому их тоже удаляем
        if (!epicsList.isEmpty()) {
            deleteAllSubTasks();
            for (int epicId : epicsList.keySet()) {
                String hisId = "e" + epicId;
                inMemoryHistoryManager.remove(hisId);
            }
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
