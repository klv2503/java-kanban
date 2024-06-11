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

    boolean deleteEpicsSubTask(Integer epicId, Integer subTaskNum);

    void deleteAllEpicsSubTask(Integer id);

    void deleteEpic(Integer id);

    void deleteAllEpics();

    void printEpicList();

    void deleteAllSubTasks();

//    void addEpicToEpic(ArrayList<SubTask> currentList, Integer dependEpic);

}
