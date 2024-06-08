package ru.taskmanagment;

import java.util.ArrayList;

public interface TaskManager {

    // Операции с задачами
    boolean isTaskExist(Integer code);

    void createTask(String name, String description);

    Task getTaskWithId(int id);

    void renewTask(Task task);

    void printTaskList();

    void removeTaskWithId(int id);

    void deleteAllTasks();

    boolean isSubTaskExist(Integer code);

    //Операции с эпиками

    boolean isEpicExist(Integer code);

    void createEpic(String name, String description, int subTasksNumber);

    //ArrayList<Task> createEpicSubTasks(Integer howTasksInEpic);

    Epic getEpicByCode(Integer code);

    void printEpicByCode(Integer code);

    void printEpicsTasks(Integer code);

    void addNewSubToEpic(Integer epicId, Integer taskId, Integer place);

    int getHowSubTasks(Integer code);

    SubTask getEpicsSubTaskByNumber(Integer epicCode, Integer subTaskNumber);

    void countEpicStatus(Integer id);

    boolean deleteEpicsSubTask(Integer epicNum, Integer subTaskNum);

    void deleteAllEpicsSubTask(Integer id);

    void deleteEpic(Integer id);

    void deleteAllEpics();

    void printEpicList();

    void deleteAllSubTasks();

    //Обобщенная очистка списков
    <T extends Task> boolean isListNotEmpty(ArrayList<T> currentArray);

    <T extends Task> void addEpicToEpic(ArrayList<T> currentList, Integer dependEpic);

}
