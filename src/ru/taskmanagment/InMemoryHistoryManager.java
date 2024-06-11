package ru.taskmanagment;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    public ArrayList<Task> history;

    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }
    @Override
    public void add(Task task) {
        if (history.size() == maximalListSize) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
