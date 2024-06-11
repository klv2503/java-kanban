package ru.taskmanagment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class EpicAdmin implements TaskManager {

    public HashMap<Integer, ArrayList<Task>> epicAdmin;

    public EpicAdmin() {
        epicAdmin = new HashMap<>();
    }

    @Override
    public boolean isEpicExist(Integer code) {
        return epicAdmin.containsKey(code);
    }

    public void put(Integer code, ArrayList<Task> currentList) {
        epicAdmin.put(code, currentList);
    }

    public void addToArray(ArrayList<Task> currentList, Task task) {
        currentList.add(task);
    }

    public ArrayList<Task> getEpicsSubTasksList(Integer code) {
        return epicAdmin.get(code);
    }

    public boolean addSubTaskInEpic(Integer epicId, Task task, Integer numInEpic) {
        //Добавить можно подзадачу или эпик по индексу numInEpic.
        //Соответственно, пока предполагается, что numInEpic >= 0
        //Если добавление невозможно, возвращаем false;
        //если список подзадач короче, чем numInEpic, добавляем task в конец списка
        if ((numInEpic < 0) || !isEpicExist(epicId)) {
            return false;
        }
        boolean resoult = true;
        int place;
        ArrayList<Task> currentList = epicAdmin.get(epicId);
        if (currentList.isEmpty()) {
            currentList.add(task);
            place = 0;
        } else if (currentList.size() < numInEpic) {
            place = currentList.size();
            currentList.add(task);
        } else {
            place = numInEpic;
            currentList.add(place, task);
        }
        if (task instanceof Epic) {
            resoult = !isEpicOwnSubTask(epicId, epicId);
            if (!resoult) {
                currentList.remove(place);
            }
        }
        epicAdmin.put(epicId, currentList);
        return resoult;
    }

    public boolean isEpicOwnSubTask(Integer mainEpicId, Integer codeForTest) {
        //Если эпик добавляется в качестве подзадачи, проверяем, не возникает ли ситуация типа "Эпик своя же подзадача"
        //в том числе опосредованно, т.е. "При добавлении в Эпик0 Эпика1 оказывается, что его позадачей является Эпик2
        //а его подзадачей является Эпик3, а его подзадачей...., а его подзадачей является Эпик0."
        //Отсюда рекурсивная проверка
        boolean resoult = false;
        ArrayList<Task> currentList = epicAdmin.get(mainEpicId);
        for (Task task : currentList) {
            if (task instanceof Epic) {
                int code = task.getCode();
                resoult = resoult || (code == codeForTest);
                if (resoult) {
                    return resoult;
                }
                resoult = isEpicOwnSubTask(code, codeForTest);
            }
        }
        return resoult;
    }

    public static void waitEnter() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Нажмите Enter");
        sc.nextLine();
    }

    @Override
    public void printEpicList() {
        if (epicAdmin == null) {
            System.out.println("Список эпиков в данный момент пуст.");
            return;
        }
        if (epicAdmin.isEmpty()) {
            System.out.println("Список эпиков в данный момент пуст.");
            return;
        }
        for (Integer code : epicAdmin.keySet()) {
            printEpicByCode(code);
        }
    }

    @Override
    public void printEpicByCode(Integer code) {
        if (isEpicExist(code)) {
            System.out.println("Выводим на экран эпик № " + code);
            printEpicsTasks(code);
        } else {
            System.out.println("Эпик с кодом " + code + " не существует.");
        }
    }

    @Override
    public void printEpicsTasks(Integer code) {
        if (isEpicExist(code)) {
            //Epic epic = epicAdmin.get(code);
            ArrayList<Task> currentList = getEpicsSubTasksList(code);
            System.out.println("Число подзадач " + currentList.size());
            if (!currentList.isEmpty()) {
                for (int i = 0; i < currentList.size(); i++) {
                    System.out.println(currentList.get(i));
                }
            } else {
                System.out.println("Нет подзадач, связанных с эпиком " + code /*+ " name: " + epic.name*/);
            }
        } else {
            System.out.println("Эпик с кодом " + code + " не существует.");
        }
    }

    @Override
    public void deleteAllEpicsSubTask(Integer id) {
        if (isEpicExist(id)) {
            ArrayList<Task> currentList = new ArrayList<>();
            epicAdmin.put(id, currentList);
        }
    }

    @Override
    public boolean deleteEpicsSubTask(Integer epicId, Integer subTaskNum) {
        if (!isEpicExist(epicId) || (subTaskNum < 0)) {
            return false;
        }
        ArrayList<Task> currentList = getEpicsSubTasksList(epicId);
        if (subTaskNum < currentList.size()) {
            currentList.remove(subTaskNum);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deleteEpic(Integer id) {
        if (isEpicExist(id)) {
            epicAdmin.remove(id);
        }
    }

    @Override
    public void deleteAllEpics() {
        epicAdmin.clear();
    }

    public boolean isEpicAdminEmpty() {
        return epicAdmin.isEmpty();
    }

    public boolean changeSubTaskStatus(Integer epicId, Integer subTaskId, Status newStatus) {
        boolean resoult = false;
        if (!isEpicExist(epicId)) {
            return resoult;
        }
        ArrayList<Task> currentList = getEpicsSubTasksList(epicId);
        if (subTaskId >= currentList.size()) {
            return resoult;
        }
        if (currentList.get(subTaskId) instanceof SubTask) {
            //У эпика статус не меняем, его можно только вычислить
            SubTask workSub = (SubTask) currentList.get(subTaskId);
            if (workSub.getStatus().equals(newStatus)) {
                return resoult;
            } else {
                workSub.setStatus(newStatus);
                currentList.set(subTaskId, workSub);
                epicAdmin.put(epicId, currentList);
                resoult = true;
            }
        }
        return resoult;
    }

    public Status countEpicsStatus(Integer epicId) {
        //Пока что для несуществующего эпика выдаем статус DONE
        Status statusCounted = Status.DONE;
        if (!isEpicExist(epicId)) {
            return statusCounted;
        }
        ArrayList<Task> currentList = getEpicsSubTasksList(epicId);
        if (currentList.isEmpty()) {
            return Status.NEW;
        }
        Status currentStatus;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i) instanceof SubTask) {
                SubTask workSub = (SubTask) currentList.get(i);
                currentStatus = workSub.getStatus();
            } else {
                Epic epic = (Epic) currentList.get(i);
                currentStatus = epic.getEpicStatus();
            }
            if (currentStatus.equals(Status.IN_PROGRESS)) {
                return Status.IN_PROGRESS;
            } else if (currentStatus.equals(Status.NEW)) {
                statusCounted = Status.NEW;
            }
        }
        return statusCounted;
    }

    @Override
    public void deleteAllSubTasks() {
        for (int code : epicAdmin.keySet()) {
            ArrayList<Task> currentList = new ArrayList<>();
            epicAdmin.put(code, currentList);
        }
    }

    @Override
    public void renewTask(Task task) {
        for (int key : epicAdmin.keySet()) {
            ArrayList<Task> currentList = getEpicsSubTasksList(key);
            if (!currentList.isEmpty()) {
                for (int i = 0; i < currentList.size(); i++) {
                    if (currentList.get(i) instanceof SubTask) {
                        SubTask subTask = (SubTask) currentList.get(i);
                        if (subTask.code == task.code) {
                            subTask.name = task.name;
                            subTask.description = task.description;
                            currentList.set(i, subTask);
                        }
                    }
                }
            }
            epicAdmin.put(key, currentList);
        }
    }

    @Override
    public void removeTaskWithId(int taskId) {
        for (int epicId : epicAdmin.keySet()) {
            ArrayList<Task> currentList = getEpicsSubTasksList(epicId);
            if (!currentList.isEmpty()) {
                int numForDelete = seekSubTaskInEpic(epicId, taskId);
                while (numForDelete >= 0) {
                    currentList.remove(numForDelete);
                    numForDelete = seekSubTaskInEpic(epicId, taskId);
                }
            }
            epicAdmin.put(epicId, currentList);
        }
    }

    public int seekSubTaskInEpic(int epicId, int subTaskId) {
        int resoult = -1;
        if (isEpicExist(epicId)) {
            ArrayList<Task> currentList = getEpicsSubTasksList(epicId);
            for (int i = 0; i < currentList.size(); i++) {
                if ((currentList.get(i) instanceof SubTask) && (subTaskId == currentList.get(i).code)) {
                    resoult = i;
                    break;
                }
            }
        }
        return resoult;
    }

    //Здесь начинаются пустые методы
    @Override
    public boolean isTaskExist(Integer code) {
        return false;
    }

    @Override
    public void createTask(String name, String description) {

    }

    @Override
    public Task getTaskWithId(int id) {
        return null;
    }


    @Override
    public void printTaskList() {

    }


    @Override
    public void deleteAllTasks() {

    }

    @Override
    public boolean isSubTaskExist(Integer code) {
        return false;
    }


    @Override
    public void createEpic(String name, String description, int subTasksNumber) {

    }

    @Override
    public Epic getEpicByCode(Integer code) {
        return null;
    }


    @Override
    public void addNewSubToEpic(Integer epicId, Integer taskId, Integer place) {

    }

    @Override
    public int getHowSubTasks(Integer code) {
        return 0;
    }

    @Override
    public SubTask getEpicsSubTaskByNumber(Integer epicCode, Integer subTaskNumber) {
        return null;
    }

    @Override
    public void countEpicStatus(Integer id) {

    }






  /*  @Override
    public void addEpicToEpic(ArrayList<SubTask> currentList, Integer dependEpic) {

    }*/
}
