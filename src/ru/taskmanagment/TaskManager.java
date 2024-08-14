package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {

    // Операции с задачами
    Task createTask(String name, String description);

    Task getTaskWithId(int id);

    void renewTask(Task task);

    void printTaskList();

    boolean removeTaskWithId(int taskId);

    void deleteAllTasks();

    List<Task> getTasksList();

    List<SubTask> getSubTasksList();

    List<Epic> getEpicsList();

    SubTask getSubTaskWithId(int id);

    boolean removeSubTaskWithId(int id);

    SubTask createSubTaskForEpic(int taskId, int epicId, LocalDateTime startTime, int duration);

    boolean isValidTime(LocalDateTime startTime, Duration duration);

    boolean removeSorted(Task task);

    void addSorted(Task task);

    int getSubTasksEpic(SubTask subTask);

    boolean makeTaskExecutable(Task task, LocalDateTime lockDT, long duration);

    //Операции с эпиками

    Epic createEpic(String name, String description, int subTasksNumber);

    void changeEpic(Epic epic);

    Epic getEpicByCode(Integer epicId);

    void printEpicByCode(Integer epicId);

    void printEpicsTasks(Integer epicId);

    void addNewSubToEpic(Integer epicId, SubTask subTask);

    int getHowSubTasks(Integer epicId);

    SubTask getEpicsSubTaskByIndex(Integer epicCode, Integer index);

    void changeSubTaskStatus(Integer epicId, Integer subNumber, Status newStat);

    void recountEpicStatus(Integer epicId);

    boolean deleteEpicsSubTask(Integer epicId, Integer index);

    void deleteAllEpicsSubTask(Integer epicId);

    boolean deleteEpic(Integer epicId);

    void deleteAllEpics();

    void printEpicList();

    void deleteAllSubTasks();

    InMemoryHistoryManager getInMemoryHistoryManager();

    List<Task> getPrioritizedTasks();
}
