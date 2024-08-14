package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
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
                startForTask(manager, manager.tasksList.get(i + 1), 15);
        }
    }

    public static void startForTask(InMemoryTaskManager manager, Task task, int duration) {
        //Поскольку Task не обязана иметь собственные startTime и duration (т.е. не выполняться вне эпиков)
        //метод устанавливает startTime и duration для Task, которая как бы эпик из одной задачи
        long startShift = TimeManager.findStartShift(duration);
        LocalDateTime locDT = TimeManager.standartStart.plus(Duration.ofMinutes(startShift));
        if (manager.isValidTime(locDT, Duration.ofMinutes(duration))) {
            task.setStartTime(locDT);
            task.setDuration(Duration.ofMinutes(duration));
            manager.sortedTasks.add(task);
        }
    }
    public void clearData(InMemoryTaskManager manager) {
        manager.deleteAllTasks();
    }
}
