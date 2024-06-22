package ru.taskmanagment;

import java.util.ArrayList;
import java.util.List;

public interface HistoryManager {
    void add(String id, Task task);
    void remove(String id);
    List<Task> getHistory();
}
