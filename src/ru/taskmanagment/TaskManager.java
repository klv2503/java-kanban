package ru.taskmanagment;

public interface TaskManager {

    // Операции с задачами
    boolean isTaskExist(Integer code);

    void createTask(String name, String description);

    Task getTaskWithId(int id);

    void renewTask(Task task);

    void printTaskList();

    void removeTaskWithId(int taskId);

    void deleteAllTasks();

    boolean isSubTaskExist(Integer code);

    //Операции с эпиками

    boolean isEpicExist(Integer epicId);

    void createEpic(String name, String description, int subTasksNumber);

    Epic getEpicByCode(Integer epicId);

    void printEpicByCode(Integer epicId);

    void printEpicsTasks(Integer epicId);

    void addNewSubToEpic(Integer epicId, Integer taskId, Integer place);

    int getHowSubTasks(Integer epicId);

    SubTask getEpicsSubTaskByIndex(Integer epicCode, Integer index);

    void countEpicStatus(Integer epicId);

    boolean deleteEpicsSubTask(Integer epicId, Integer index);

    void deleteAllEpicsSubTask(Integer epicId);

    void deleteEpic(Integer epicId);

    void deleteAllEpics();

    void printEpicList();

    void deleteAllSubTasks();

}
