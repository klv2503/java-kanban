package ru.taskmanagment;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    //Тестируем только работу с файлами
    static String nameOfTestFile = "file4Test.txt";
    static FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(nameOfTestFile);
    static int numberOfGeneratedTasks = 4;
    static int numberOfGeneratedEpics = 2;
    static Random rnd = new Random();

    @Test
    public void testIfFileBackedTaskManagerWritesAndReadEmptyFile() {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(nameOfTestFile);
        //проверяем, пуст ли массив с состоянием менеджера
        String errorMessage = "Список с состоянием менеджера не пуст";
        boolean isCorrect = fileBackedTaskManager.dataToSave.isEmpty();
        assertEquals(true, isCorrect, errorMessage);
        fileBackedTaskManager.taskCounter = 0;
        fileBackedTaskManager.subTaskCounter = 0;
        fileBackedTaskManager.epicCounter = 0;
        fileBackedTaskManager.tasksList.clear();
        fileBackedTaskManager.subTasksList.clear();
        fileBackedTaskManager.epicsList.clear();
        try {
            fileBackedTaskManager.fileName = Files.createTempFile("TestTmp4Manager", ".txt");
            fileBackedTaskManager.save();
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка сохранения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            fileBackedTaskManager.loadFromFile(fileBackedTaskManager.fileName.toFile());
            Files.delete(fileBackedTaskManager.fileName);
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка чтения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        //Поскольку первой записью пишется состояние счетчиков,
        //при пустых списках объектов в файле должна быть ровно одна запись, а именно "0,0,0"
        int dataSize = fileBackedTaskManager.dataToSave.size();
        errorMessage = "Число строк в файле не равно 1";
        assertEquals(1, dataSize, errorMessage);
        isCorrect = fileBackedTaskManager.dataToSave.get(0).equals("0,0,0");
        errorMessage = "Строка состояния не соответствует ожидаемой";
        assertEquals(true, isCorrect, errorMessage);
    }

    @Test
    public void createSomeTaskAndEpicAndWriteIntoFileAndReadFile() {
        fileBackedTaskManager.deleteAllTasks();
        for (int i = 0; i < numberOfGeneratedTasks; i++) {
            fileBackedTaskManager.makeTask("Shortly", "Long description");
        }
        for (int i = 0; i < numberOfGeneratedEpics; i++) {
            String name = "Epic #";
            String description = "Description of Epic #";
            int taskNumber = i + 2;
            fileBackedTaskManager.makeTestEpic(name, description, taskNumber);
        }
        String errorMessage = "Список с состоянием содержит неверное количество строк";
        int expectedSize = fileBackedTaskManager.taskCounter + fileBackedTaskManager.epicCounter
                + fileBackedTaskManager.subTaskCounter + 1;
        int realSize = fileBackedTaskManager.dataToSave.size();
        assertEquals(expectedSize, realSize, errorMessage);
        try {
            fileBackedTaskManager.fileName = Files.createTempFile("TestTmp4Manager", ".txt");
            fileBackedTaskManager.save();
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка сохранения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        //После записи удаляем всё так, чтобы не было перезаписи в файл и обнуляем счетчики
        fileBackedTaskManager.taskCounter = 0;
        fileBackedTaskManager.subTaskCounter = 0;
        fileBackedTaskManager.epicCounter = 0;
        fileBackedTaskManager.tasksList.clear();
        fileBackedTaskManager.subTasksList.clear();
        fileBackedTaskManager.epicsList.clear();
        try {
            fileBackedTaskManager.loadFromFile(fileBackedTaskManager.fileName.toFile());
            Files.delete(fileBackedTaskManager.fileName);
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка чтения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        //После чтения и восстановления проверяем счетчики
        errorMessage = "Значение счетчика задач не совпадает с ожидаемым";
        assertEquals(4, fileBackedTaskManager.taskCounter, errorMessage);
        errorMessage = "Значение счетчика подзадач не совпадает с ожидаемым";
        assertEquals(5, fileBackedTaskManager.subTaskCounter, errorMessage);
        errorMessage = "Значение счетчика эпиков не совпадает с ожидаемым";
        assertEquals(2, fileBackedTaskManager.epicCounter, errorMessage);
        // считаем и сверяем количество экземпляров объектов
        int taskNum = 0;
        for (int i : fileBackedTaskManager.tasksList.keySet())
            taskNum++;
        errorMessage = "Количество задач не совпадает с ожидаемым";
        assertEquals(4, taskNum, errorMessage);
        int epicNum = 0;
        for (int i : fileBackedTaskManager.epicsList.keySet())
            epicNum++;
        errorMessage = "Количество эпиков не совпадает с ожидаемым";
        assertEquals(2, epicNum, errorMessage);
        int subTaskNum = 0;
        for (int i : fileBackedTaskManager.subTasksList.keySet())
            subTaskNum++;
        errorMessage = "Количество подзадач не совпадает с ожидаемым";
        assertEquals(5, subTaskNum, errorMessage);
    }
}
