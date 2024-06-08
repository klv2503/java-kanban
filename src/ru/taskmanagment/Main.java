package ru.taskmanagment;

import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();

        System.out.println("Поехали!");
        // Test0
        System.out.println("Тест0: проверка стартового состояния списка задач и эпиков");
        inMemoryTaskManager.printTaskList(); //проверяем исходное состояние списков задач и эпиков
        inMemoryTaskManager.printEpicList();
        inMemoryTaskManager.printHistory();
        waitEnter();

        //генерация тестовых данных
        int numberOfGeneratedTasks = 10;
        int numberOfGeneratedEpics = 5;
        generateTestData(inMemoryTaskManager, numberOfGeneratedTasks, numberOfGeneratedEpics);
        // Тесты со списком задач
        // Test1.1
        System.out.println("Задачи Тест 1: Распечатать список задач:");
        inMemoryTaskManager.printTaskList();
        waitEnter();

        // Test1.2
        System.out.println("Задачи Тест 2: создать задачу:");
        inMemoryTaskManager.createTask("New task", "Creation test");
        System.out.println(inMemoryTaskManager.getTaskWithId(inMemoryTaskManager.getTaskCounter()));
        waitEnter();
        System.out.println("Выводим историю просмотров.");
        inMemoryTaskManager.printHistory();
        waitEnter();

        // Test1.3
        System.out.println("Задачи Тест 3: получить задачу по идентификатору:");
        if (inMemoryTaskManager.isTaskExist(3)) {
            System.out.println(inMemoryTaskManager.getTaskWithId(3));
        } else {
            System.out.println("Нет такой задачи");
        }
        waitEnter();

        // Test1.4
        System.out.println("Задачи Тест 4: обновить задачу по идентификатору:");
        int idRenewTask = 1;
        Task task = new Task(idRenewTask, "New name", "New description");
        if (inMemoryTaskManager.isTaskExist(idRenewTask)) {
            System.out.println("Исходная задача:" + "\n" + inMemoryTaskManager.getTaskWithId(idRenewTask) + "\n"
                    + "Новая задача:");
            inMemoryTaskManager.renewTask(task);
            System.out.println(inMemoryTaskManager.getTaskWithId(idRenewTask));
        } else {
            System.out.println("Такой задачи в списке нет");
        }
        waitEnter();

// Test1.5
        System.out.println("Задачи Тест 5: удалить задачу по идентификатору:");
        if (inMemoryTaskManager.isTaskExist(4)) {
            System.out.println("Удаляем задачу " + 4);
            inMemoryTaskManager.removeTaskWithId(4);
        } else {
            System.out.println("Такой задачи не было");
        }
        System.out.println("Выводим историю просмотров.");
        inMemoryTaskManager.printHistory();
        System.out.println("Теперь имеем такой список задач:");
        inMemoryTaskManager.printTaskList();
        waitEnter();

        // Конец тестирования списка задач. Примечание: тест по очистке списка задач перенесен в конец метода
        // чтобы не генерировать задачи повторно для тестирования эпиков
        // Тестирование списка эпиков

        // Test 2.1
        System.out.println("Эпики Тест 1: Список эпиков с их подзадачами:");
        inMemoryTaskManager.printEpicList();
        waitEnter();

        // Test2.2
        System.out.println("Эпики Тест 2: создать новый пустой эпик:");
        int newEpicNum = inMemoryTaskManager.getEpicCounter() + 1;
        String name = "Epic № " + newEpicNum;
        String description = "Test of creation № " + newEpicNum;
        inMemoryTaskManager.createEpic(name, description, 0);
        inMemoryTaskManager.printEpicByCode(inMemoryTaskManager.getEpicCounter());
        waitEnter();

        // Test2.3
        System.out.println("Эпики Тест 3: получить эпик по идентификатору:");
        int id = 3;
        Epic workEpic = inMemoryTaskManager.getEpicByCode(id);
        if (workEpic == null) {
            System.out.println("Нет эпика с кодом " + id);
        } else {
            inMemoryTaskManager.printEpicByCode(id);
        }
        waitEnter();

        // Test 2.4 Операции с подзадачами
        // Тест 2.4.1
        System.out.println("Эпики Тест 4.1: Изменить в эпике name у первых двух его подзадач статусы:");
        id = inMemoryTaskManager.getSpecificEpic(); //ищем эпик, у которого больше двух подзадач, берем первый попавшийся
        // Примечание: из-за того, что число подзадач случайно, возможно отсутствие нужного эпика
        if (id > 0) {
            workEpic = inMemoryTaskManager.getEpicByCode(id);
            System.out.println("Исходный эпик:");
            inMemoryTaskManager.printEpicByCode(id);
            System.out.println("Эпик с новыми задачами:");
            inMemoryTaskManager.changeSubTaskStatus(id, 1, Status.DONE);
            inMemoryTaskManager.changeSubTaskStatus(id, 2, Status.IN_PROGRESS);
        }
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

// Тест 2.4.2
        System.out.println("Эпики Тест 4.2: Пересчитать статус эпика:");
        inMemoryTaskManager.countEpicStatus(id);
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

        // Тест 2.4.3
        System.out.println("Эпики Тест 4.3: удаление одной подзадачи эпика:");
        id = 1;
        int stNum = 3;
        System.out.println("Текущее состояние эпика:");
        inMemoryTaskManager.printEpicByCode(id);
        if (inMemoryTaskManager.deleteEpicsSubTask(id, stNum)) {
        //    inMemoryTaskManager.renumEpicsSubTask(id);
            System.out.println("Удаление выполнено. Новое состояние эпика:");
            inMemoryTaskManager.printEpicByCode(id);
        } else System.out.println("Подзадачи " + stNum + " в эпике № " + id + " не было.");
        waitEnter();

        // Тест 2.4.4
        System.out.println("Эпики Тест 4.4: удаление всех подзадач эпика:");
        System.out.println("Текущее состояние эпика:");
        inMemoryTaskManager.printEpicByCode(id);
        inMemoryTaskManager.deleteAllEpicsSubTask(id);
        System.out.println("Удаление выполнено. Новое состояние эпика:");
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

        // Тест 2.4.5
        System.out.println("Эпики Тест 4.5: Добавляем задачи в очищенный эпик:");
        inMemoryTaskManager.addNewSubToEpic(id, 2, 0);
        inMemoryTaskManager.addNewSubToEpic(id, 5, 1);
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

        // Тест 2.4.6
        System.out.println("Эпики Тест 4.6: А теперь добавляем задачу в середину списка подзадач:");
        System.out.println("Исходный эпик:");
        inMemoryTaskManager.printEpicByCode(id);
        inMemoryTaskManager.addNewSubToEpic(id, 8, 1);
        System.out.println("Тот же эпик после вставки:");
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

        // Специальный тест: пробуем добавить эпик как подзадачу в эпик
        System.out.println("Специальный тест: пробуем добавить эпик как подзадачу в эпик");
        System.out.println("Исходный эпик:");
        id = 2;
        int id2 = 3;
        inMemoryTaskManager.printEpicByCode(id);
        inMemoryTaskManager.addEpicAsSubTask(id, id2);
        System.out.println("Тот же эпик после добавления:");
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

        // Тест 2.4.6 выбрать подзадачу у конкретного эпика
        System.out.println("Эпики Тест 4.7: Получаем подзадачу конкретного эпика по ее номеру в эпике");
        id = 2;
        int idSubTask = 5;
        SubTask tstSubTask = inMemoryTaskManager.getEpicsSubTaskByNumber(id, idSubTask);
        if (tstSubTask == null) {
            System.out.println("Нет эпика с кодом " + id + " или его подзадачи " + idSubTask);
        } else System.out.println(tstSubTask);
        System.out.println("Выводим историю просмотров.");
        inMemoryTaskManager.printHistory();
        waitEnter();

        // Тест 2.5
        System.out.println("Эпики Тест 5: Удаление эпика:");
        id = 5;
        System.out.println("Планируем удалить эпик с кодом " + id + " его текущее состояние:");
        inMemoryTaskManager.printEpicByCode(id);
        inMemoryTaskManager.deleteEpic(id);
        System.out.println("Пытаемся вывести на экран эпик № " + id);
        inMemoryTaskManager.printEpicByCode(id);
        waitEnter();

        // Тест 2.6
        System.out.println("Эпики Тест 6: Удаление всех подзадач. Исходный список эпиков:");
        inMemoryTaskManager.printEpicList();
        waitEnter();

        System.out.println("Для проверки выводим список эпиков с подзадачами.");
        inMemoryTaskManager.deleteAllSubTasks();
        inMemoryTaskManager.printEpicList();
        waitEnter();

        System.out.println("Эпики Тест 6: Удаление всех эпиков. Для проверки выводим текущий список эпиков");
        inMemoryTaskManager.deleteAllEpics();
        inMemoryTaskManager.printEpicList();
        waitEnter();

        // Test1.6
        System.out.println("Задачи Тест 6: удалить все задачи");
        System.out.println("Исходный список задач:");
        inMemoryTaskManager.printTaskList();
        System.out.println("Исходный список подзадач:");
        inMemoryTaskManager.printSubTaskList();
        waitEnter();

        inMemoryTaskManager.deleteAllTasks();
        System.out.println("Проверяем, пуст ли список задач после очистки:");
        inMemoryTaskManager.printTaskList();
        System.out.println("Проверяем, пуст ли список подзадач после очистки:");
        inMemoryTaskManager.printSubTaskList();
        waitEnter();

        System.out.println("Выводим итоговую историю просмотров.");
        System.out.println("getTaskCall= " + inMemoryTaskManager.getTaskCall
                + " getSubTaskCall= " + inMemoryTaskManager.getSubTaskCall
                + " getEpicCall= " + inMemoryTaskManager.getEpicCall);
        inMemoryTaskManager.printHistory();
        waitEnter();
    }

    public static void generateTestData(InMemoryTaskManager inMemoryTaskManager, int taskNum, int epicNum) {
        for (int i = 0; i < taskNum; i++) {
            inMemoryTaskManager.makeTask("Shortly", "Long description");
        }
        Random rnd = new Random();
        for (int i = 0; i < epicNum; i++) {
            String name = "Epic #";
            String description = "Description of Epic #";
            int taskNumber = rnd.nextInt(taskNum - 1) + 1;
            inMemoryTaskManager.makeTestEpic(name, description, taskNumber);
        }
    }

    public static void waitEnter() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Нажмите Enter");
        sc.nextLine();
    }
}
