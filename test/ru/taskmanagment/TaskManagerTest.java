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
            //В целях тестирования PrioritizedTasks три task (№1, №3, №5) делаем выполняемыми
            if (i % 2 == 0)
                manager.makeTaskExecutable(manager.tasksList.get(i + 1), 15);
        }
    }

    public void clearData(InMemoryTaskManager manager) {
        manager.deleteAllTasks();
    }
}
