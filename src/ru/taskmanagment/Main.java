package ru.taskmanagment;

import java.io.File;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        File fileName = Paths.get(System.getProperty("user.home"), "managesaver.txt").toFile();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(fileName);
        //Если существует файл со старым состоянием менеджера, то пытаемся менеджера восстановить и проверить.
        //Иначе генерируем данные и проходим тесты
        if (fileName.exists()) {
            //Проверяем, получилось ли восстановить. Для этого выводим на экран списки и сверяем с файлом
            System.out.println("taskCounter = " + manager.getTaskCounter()
                    + " subTaskCounter = " + manager.getSubTaskCounter()
                    + " epicCounter = " + manager.getEpicCounter());
            manager.printTaskList();
            manager.printEpicList();
            manager.printHistory();
            waitEnter();
            //добавлена проверка методов с TreeSet
            manager.getPrioritizedTasks();
            manager.printPrioritizedTasks();
            waitEnter();
            return;
        }
        // Test0
        System.out.println("Тест0: проверка стартового состояния списка задач и эпиков");
        manager.printTaskList(); //проверяем исходное состояние списков задач и эпиков
        manager.printEpicList();
        manager.printHistory();
        waitEnter();
        //генерация тестовых данных
        int numberOfGeneratedTasks = 10;
        int numberOfGeneratedEpics = 5;
        generateTestData(manager, numberOfGeneratedTasks, numberOfGeneratedEpics);
        // Тесты со списком задач
        // Test1.1
        System.out.println("Задачи Тест 1: Распечатать список задач:");
        manager.printTaskList();
        manager.printEpicList();
        waitEnter();
        // Test1.2
        System.out.println("Задачи Тест 2: создать задачу:");
        manager.createTask("New task", "Creation test");
        System.out.println(manager.getTaskWithId(manager.getTaskCounter()));
        waitEnter();
        System.out.println("Выводим историю просмотров.");
        manager.printHistory();
        waitEnter();
        // Test1.3
        System.out.println("Задачи Тест 3: получить задачу по идентификатору:");
        if (manager.tasksList.containsKey(3)) {
            System.out.println(manager.getTaskWithId(3));
        } else {
            System.out.println("Нет такой задачи");
        }
        waitEnter();
        // Test1.4
        System.out.println("Задачи Тест 4: обновить задачу по идентификатору:");
        int idRenewTask = 1;
        Task task = new Task(idRenewTask, "New name", "New description");
        if (manager.tasksList.containsKey(idRenewTask)) {
            System.out.println("Исходная задача:" + "\n" + manager.getTaskWithId(idRenewTask) + "\n"
                    + "Новая задача:");
            manager.renewTask(task);
            System.out.println(manager.getTaskWithId(idRenewTask));
        } else {
            System.out.println("Такой задачи в списке нет");
        }
        waitEnter();
        // Test1.5
        System.out.println("Задачи Тест 5: удалить задачу по идентификатору:");
        if (manager.tasksList.containsKey(4)) {
            System.out.println("Удаляем задачу " + 4);
            manager.removeTaskWithId(4);
        } else {
            System.out.println("Такой задачи не было");
        }
        System.out.println("Выводим историю просмотров.");
        manager.printHistory();
        System.out.println("Теперь имеем такой список задач:");
        manager.printTaskList();
        waitEnter();
        // Тестирование списка эпиков
        // Test 2.1
        System.out.println("Эпики Тест 1: Список эпиков с их подзадачами:");
        manager.printEpicList();
        waitEnter();
        // Test2.2
        System.out.println("Эпики Тест 2: создать новый пустой эпик:");
        int newEpicNum = manager.getEpicCounter() + 1;
        String name = "Epic № " + newEpicNum;
        String description = "Test of creation № " + newEpicNum;
        manager.createEpic(name, description, 0);
        manager.printEpicByCode(manager.getEpicCounter());
        waitEnter();
        // Test2.3
        System.out.println("Эпики Тест 3: получить эпик по идентификатору:");
        int id = 3;
        Epic workEpic = manager.getEpicByCode(id);
        if (workEpic == null) {
            System.out.println("Нет эпика с кодом " + id);
        } else {
            manager.printEpicByCode(id);
        }
        waitEnter();
        // Test 2.4 Операции с подзадачами
        // Тест 2.4.1
        System.out.println("Эпики Тест 4.1: Изменить в эпике name у двух его подзадач статусы:");
        id = manager.getSpecificEpic(); //ищем эпик, у которого больше двух подзадач, берем первый
        // попавшийся. Примечание: из-за того, что число подзадач случайно, возможно отсутствие нужного эпика
        if (id >= 0) {
            System.out.println("Исходный эпик:");
            manager.printEpicByCode(id);
            manager.changeSubTaskStatus(id, 1, Status.DONE);
            manager.changeSubTaskStatus(id, 2, Status.IN_PROGRESS);
        }
        System.out.println("Эпик с новыми задачами:");
        manager.printEpicByCode(id);
        waitEnter();
        // Тест 2.4.2
        System.out.println("Эпики Тест 4.2: Пересчитать статус эпика:");
        manager.recountEpicStatus(id);
        manager.printEpicByCode(id);
        waitEnter();
        // Тест 2.4.3
        System.out.println("Эпики Тест 4.3: удаление одной подзадачи эпика:");
        id = 1;
        int stNum = 3;
        System.out.println("Текущее состояние эпика:");
        manager.printEpicByCode(id);
        if (manager.deleteEpicsSubTask(id, stNum)) {
            System.out.println("Удаление выполнено. Новое состояние эпика:");
            manager.printEpicByCode(id);
        } else {
            System.out.println("Подзадачи " + stNum + " в эпике № " + id + " не было.");
        }
        waitEnter();
        // Тест 2.4.4
        System.out.println("Эпики Тест 4.4: удаление всех подзадач эпика:");
        System.out.println("Текущее состояние эпика:");
        manager.printEpicByCode(id);
        manager.deleteAllEpicsSubTask(id);
        System.out.println("Удаление выполнено. Новое состояние эпика:");
        manager.printEpicByCode(id);
        waitEnter();
        // Тест 2.4.5
        System.out.println("Эпики Тест 4.5: Добавляем задачи в очищенный эпик:");
        manager.addNewSubToEpic(id, 2, 0);
        manager.addNewSubToEpic(id, 5, 1);
        manager.printEpicByCode(id);
        waitEnter();
        // Тест 2.4.6
        System.out.println("Эпики Тест 4.6: А теперь добавляем задачу в середину списка подзадач:");
        System.out.println("Исходный эпик:");
        manager.printEpicByCode(id);
        manager.addNewSubToEpic(id, 8, 1);
        System.out.println("Тот же эпик после вставки:");
        manager.printEpicByCode(id);
        waitEnter();
        // Тест 2.4.6 выбрать подзадачу у конкретного эпика
        System.out.println("Эпики Тест 4.7.1: Получаем подзадачу конкретного эпика по ее номеру в эпике");
        id = 2;
        int idSubTask = 5;
        SubTask tstSubTask = manager.getEpicsSubTaskByIndex(id, idSubTask);
        if (tstSubTask == null) {
            System.out.println("Нет эпика с кодом " + id + " или в нем нет подзадачи по индексу " + idSubTask);
        } else {
            System.out.println(tstSubTask);
        }
        System.out.println("Эпики Тест 4.7.2: Получаем подзадачу конкретного эпика по ее Id");
        id = 4;
        idSubTask = 8;
        int index = manager.seekSubTaskInEpic(id, idSubTask);
        if (index == -1) {
            System.out.println("Нет эпика с кодом " + id + " или в нем нет подзадачи с кодом " + idSubTask);
        } else {
            SubTask subTask = manager.getEpicsSubTaskByIndex(id, index);
            System.out.println(subTask);
        }
        System.out.println("Выводим историю просмотров.");
        manager.printHistory();
        waitEnter();
        // Тест 2.5
        System.out.println("Эпики Тест 5: Удаление эпика:");
        id = 5;
        System.out.println("Планируем удалить эпик с кодом " + id + " его текущее состояние:");
        manager.printEpicByCode(id);
        manager.deleteEpic(id);
        System.out.println("Пытаемся после удаления вывести на экран эпик № " + id);
        manager.printEpicByCode(id);
        waitEnter();
    }

    public static void generateTestData(InMemoryTaskManager manager, int taskNum, int epicNum) {
        for (int i = 0; i < taskNum; i++) {
            manager.makeTask("Shortly", "Long description");
        }
        Random rnd = new Random();
        for (int i = 0; i < epicNum; i++) {
            String name = "Epic #";
            String description = "Description of Epic #";
            int taskNumber = rnd.nextInt(taskNum - 1) + 1;
            manager.makeTestEpic(name, description, taskNumber);
        }
    }

    public static void waitEnter() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Нажмите Enter");
        sc.nextLine();
    }
}
