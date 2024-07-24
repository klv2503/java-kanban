package ru.taskmanagment;

import java.util.Random;

abstract class TaskManagerTest<T extends TaskManager> {

    static int numberOfGeneratedTasks = 10;
    static int numberOfGeneratedEpics = 6;
    static Random rnd = new Random();

    public static void dataGeneration(InMemoryTaskManager manager) {
        for (int i = 0; i < numberOfGeneratedTasks; i++) {
            manager.makeTask("Shortly", "Long description");
        }
        for (int i = 0; i < numberOfGeneratedEpics; i++) {
            String name = "Epic #";
            String description = "Description of Epic #";
            int taskNumber = rnd.nextInt(numberOfGeneratedTasks - 1) + 1;
            manager.makeTestEpic(name, description, taskNumber);
        }
    }

    public void clearData(InMemoryTaskManager manager) {
        manager.deleteAllTasks();
    }
}
