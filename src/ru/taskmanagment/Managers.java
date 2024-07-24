package ru.taskmanagment;

import java.io.File;

public class Managers {

    private Managers() {

    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager FileBackedTaskManager(File file) {
        return new FileBackedTaskManager(file);
    }

    static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
