package ru.taskmanagment;

import java.util.ArrayList;

public interface HistoryManager {
    int maximalListSize = 10;
    void add(Task task);
    ArrayList<Task> getHistory();
}
