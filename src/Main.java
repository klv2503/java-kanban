import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        Scanner sc = new Scanner(System.in);
        String help;

        System.out.println("Поехали!");
        // Test0
        System.out.println("Тест0: проверка стартового состояния списка задач");
        taskManager.printTaskList();

        //генерация тестовых задач
        for (int i = 0; i < 5; i++) {
            Task task = new Task("Shortly", "Long description");
            task = taskManager.makeTask(task);
        }
        // Тесты со списком задач
        // Test1.1
        System.out.println("Задачи Тест 1: Распечатать список задач:");
        taskManager.printTaskList();
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test1.2
        System.out.println("Задачи Тест 2: создать задачу:");
        Task task = new Task("New task", "Creation test");
        taskManager.createTask(task);
        System.out.println(taskManager.getTaskWithId(6));

        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test1.3
        System.out.println("Задачи Тест 3: получить задачу по идентификатору:");
        if (taskManager.isTaskExist(3)) {
            System.out.println(taskManager.getTaskWithId(3));
        } else {
            System.out.println("Нет такой задачи");
        }
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test1.4
        System.out.println("Задачи Тест 4: обновить задачу по идентификатору:");
        int idRenewTask = 1;
        task.code = idRenewTask;
        task.name = "New name";
        task.description = "New description";
        if (taskManager.isTaskExist(idRenewTask)) {
            System.out.println("Исходная задача:" + "\n" + taskManager.getTaskWithId(1) + "\n" + "Новая задача:");
            taskManager.renewTask(task);
            System.out.println(taskManager.getTaskWithId(1));
        } else {
            System.out.println("Такой задачи в списке нет");
        }
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

// Test1.5
        System.out.println("Задачи Тест 5: удалить задачу по идентификатору:");
        if (taskManager.isTaskExist(4)) {
            System.out.println("Удаляем задачу " + 4);
            taskManager.removeTaskWithId(4);
        } else {
            System.out.println("Такой задачи не было");
        }
        System.out.println("Теперь имеем такой список задач:");
        taskManager.printTaskList();
        System.out.println("Нажмите Enter");
        help = sc.nextLine();
        // Конец тестирования списка задач. Примечание: тест по очистке списка задач перенесен в конец метода
        // чтобы не генерировать задачи повторно для тестирования эпиков

        // Тестирование списка эпиков

        for (int i = 0; i < 4; i++) {
            String epicName = "Epic #";
            taskManager.makeTestEpic(epicName);
        }
        // конец генерации тестовых данных

        // Test 2.1
        System.out.println("Эпики Тест 1: Список эпиков с их подзадачами:");
        taskManager.printEpicList();
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test2.2
        System.out.println("Эпики Тест 2: создать новый эпик:");
        Epic workEpic = new Epic("Epic №", Status.NEW);
        taskManager.createEpic(workEpic);
        taskManager.printEpicByCode(5);
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test2.3
        System.out.println("Эпики Тест 3: получить эпик по идентификатору:");
        int id = 3;
        workEpic = taskManager.getEpicByCode(id);
        if (workEpic == null) {
            System.out.println("Нет такого эпика");
        } else {
            System.out.println(workEpic);
        }
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test 2.4 Операции с подзадачами
        // Тест 2.4.1
        System.out.println("Эпики Тест 4.1: Изменить в эпике epicName у первых двух его подзадач статусы:");
        id = taskManager.getSpecificEpic(); //ищем эпик, у которого больше двух подзадач, берем первый попавшийся
        // Примечание: из-за того, что число подзадач случайно, возможно отсутствие нужного эпика
        if (id > 0) {
            workEpic = taskManager.getEpicByCode(id);
            System.out.println("Исходный эпик:");
            taskManager.printEpicByCode(id);
            System.out.println("Новая задача:");
            String name = workEpic.getEpicName();
            taskManager.changeSubTaskStatus(name, 1, Status.DONE);
            taskManager.changeSubTaskStatus(name, 2, Status.IN_PROGRESS);
        }
        taskManager.printEpicByCode(id);
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

// Тест 2.4.2
        System.out.println("Эпики Тест 4.2: Пересчитать статус эпика:");
        taskManager.countEpicStatus(id);
        taskManager.printEpicByCode(id);
        System.out.println("Нажмите Enter");
        help = sc.nextLine();
        // Тест 2.4.3
        System.out.println("Эпики Тест 4.3: удаление всех подзадач эпика:");
        id = 1;
        System.out.println("Текущее состояние эпика:");
        taskManager.printEpicByCode(id);
        taskManager.clearEpicsSub(id);
        System.out.println("Удаление выполнено. Новое состояние эпика:");
        taskManager.printEpicByCode(id);
        System.out.println("Нажмите Enter");
        help = sc.nextLine();
        // Тест 2.4.4
        System.out.println("Эпики Тест 4.4: Добавляем задачи в очищенный эпик:");
        taskManager.addNewSubToEpic(id, 2);
        taskManager.addNewSubToEpic(id, 5);
        taskManager.printEpicByCode(id);
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Тест 2.5
        System.out.println("Эпики Тест 5: Удаление эпика:");
        id = 5;
        System.out.println("Планируем удалить эпик с кодом " + id + " его текущее состояние:");
        taskManager.printEpicByCode(id);
        taskManager.deleteEpic(id);
        System.out.println("Пытаемся вывести на экран эпик № " + id);
        taskManager.printEpicByCode(id);
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Тест 2.6
        System.out.println("Эпики Тест 6: Удаление всех подзадач. Для проверки выводим список эпиков с подзадачами.");
        taskManager.clearSubTasks();
        taskManager.printEpicList();
        System.out.println("Нажмите Enter");
        help = sc.nextLine();
        System.out.println("Эпики Тест 6: Удаление всех эпиков. Для проверки выводим текущий список эпиков");
        taskManager.clearEpics();
        taskManager.printEpicList();
        System.out.println("Нажмите Enter");
        help = sc.nextLine();

        // Test1.6
        System.out.println("Задачи Тест 6: удалить все задачи");
        System.out.println("Исходный список:");
        taskManager.printTaskList();
        taskManager.deleteAllTasks();
        System.out.println("Проверяем, пуст ли список после очистки:");
        taskManager.printTaskList();
    }

}
