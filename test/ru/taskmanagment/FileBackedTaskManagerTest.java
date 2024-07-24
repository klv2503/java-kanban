package ru.taskmanagment;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest {
    //Тестируем только работу с файлами
    static File nameOfTestFile = Paths.get(System.getProperty("user.home"), "file4Test.txt").toFile();
    static FileBackedTaskManager manager = new FileBackedTaskManager(nameOfTestFile);
    static int numberOfGeneratedTasks = 4;
    static int numberOfGeneratedEpics = 2;

    @Test
    public void testIfFileBackedTaskManagerWritesAndReadEmptyFile() {
        FileBackedTaskManager manager = new FileBackedTaskManager(nameOfTestFile);
        //проверяем, пуст ли массив с состоянием менеджера
        String errorMessage = "Список с состоянием менеджера не пуст";
        boolean isCorrect = manager.dataToSave.isEmpty();
        assertEquals(true, isCorrect, errorMessage);
        manager.taskCounter = 0;
        manager.subTaskCounter = 0;
        manager.epicCounter = 0;
        manager.tasksList.clear();
        manager.subTasksList.clear();
        manager.epicsList.clear();
        try {
            manager.fileName = Files.createTempFile("TestTmp4Manager", ".txt").toFile();
            manager.save();
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка сохранения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            manager.loadFromFile(manager.fileName);
            Files.delete(manager.fileName.toPath());
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка чтения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        //Поскольку первой записью пишется строка заголовков,
        //при пустых списках объектов в файле должна быть ровно одна запись, а именно перечень полей для CSV
        int dataSize = manager.dataToSave.size();
        errorMessage = "Число строк в файле не равно 1";
        assertEquals(1, dataSize, errorMessage);
        isCorrect = manager.dataToSave.get(0)
                .equals("id,type,name,status,description,startTime,duration,endTime,parentTask,epic");
        errorMessage = "Строка состояния не соответствует ожидаемой";
        assertEquals(true, isCorrect, errorMessage);
    }

    @Test
    public void createSomeTaskAndEpicAndWriteIntoFileAndReadFile() {
        manager.deleteAllTasks();
        for (int i = 0; i < numberOfGeneratedTasks; i++) {
            manager.makeTask("Shortly", "Long description");
        }
        for (int i = 0; i < numberOfGeneratedEpics; i++) {
            String name = "Epic #";
            String description = "Description of Epic #";
            int taskNumber = i + 2;
            manager.makeTestEpic(name, description, taskNumber);
        }
        String errorMessage = "Список с состоянием содержит неверное количество строк";
        int expectedSize = manager.taskCounter + manager.epicCounter
                + manager.subTaskCounter + 1;
        int realSize = manager.dataToSave.size();
        assertEquals(expectedSize, realSize, errorMessage);
        try {
            manager.fileName = Files.createTempFile("TestTmp4Manager", ".txt").toFile();
            manager.save();
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка сохранения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        //После записи удаляем всё так, чтобы не было перезаписи в файл и обнуляем счетчики
        manager.taskCounter = 0;
        manager.subTaskCounter = 0;
        manager.epicCounter = 0;
        manager.tasksList.clear();
        manager.subTasksList.clear();
        manager.epicsList.clear();
        try {
            manager.loadFromFile(manager.fileName);
            Files.delete(manager.fileName.toPath());
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка чтения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        //После чтения и восстановления проверяем счетчики
        errorMessage = "Значение счетчика задач не совпадает с ожидаемым";
        assertEquals(4, manager.taskCounter, errorMessage);
        errorMessage = "Значение счетчика подзадач не совпадает с ожидаемым";
        assertEquals(5, manager.subTaskCounter, errorMessage);
        errorMessage = "Значение счетчика эпиков не совпадает с ожидаемым";
        assertEquals(2, manager.epicCounter, errorMessage);
        // считаем и сверяем количество экземпляров объектов
        int taskNum = 0;
        for (int i : manager.tasksList.keySet())
            taskNum++;
        errorMessage = "Количество задач не совпадает с ожидаемым";
        assertEquals(4, taskNum, errorMessage);
        int epicNum = 0;
        for (int i : manager.epicsList.keySet())
            epicNum++;
        errorMessage = "Количество эпиков не совпадает с ожидаемым";
        assertEquals(2, epicNum, errorMessage);
        int subTaskNum = 0;
        for (int i : manager.subTasksList.keySet())
            subTaskNum++;
        errorMessage = "Количество подзадач не совпадает с ожидаемым";
        assertEquals(5, subTaskNum, errorMessage);
        clearData(manager);
    }

    @Test
    void whenAssertingException() {
        String message = "Сгенерировано непроверемое исключение";
        Exception exception = assertThrows(
                ManagerSaveException.class,
                () -> {
                    throw new ManagerSaveException(message);
                }
        );
        assertEquals(message, exception.getMessage());
    }
}
