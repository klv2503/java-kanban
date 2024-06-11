package ru.taskmanagment;

public class Managers {

    private Managers() {

    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    };
}
