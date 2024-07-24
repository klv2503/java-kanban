package ru.taskmanagment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileBackedTaskManager extends InMemoryTaskManager {

    static File fileName;
    final String home = System.getProperty("user.home");
    //Пока список тупо перезаписывается при вызове метода save.
    //Скорее всего, список переделаю в двусвязную мапу по образцу хранения истории.
    static List<String> dataToSave;

    public FileBackedTaskManager(File fileToSave) {
        fileName = fileToSave;
        dataToSave = new ArrayList<>();
    }

    public void prepareDataToSave() {
        //Прочитал про стандарт формата CSV, решил сделать файл согласно стандарту.
        //Поэтому заголовки пишутся в файл, а значения счетчиков высчитываются при загрузке.
        //Добавил новые поля согласно ТЗ к Спринту8.
        //Класс CSVFormatter сплагиатил у наставника. Заодно нашлось более симпатичное (как мне кажется)
        //решение с преобразованием задач в строки.
        dataToSave.clear();
        dataToSave.add("id,type,name,status,description,startTime,duration,endTime,parentTask,epic\n");
        dataToSave.addAll(
                tasksList.keySet().stream()
                        .map(i -> tasksList.get(i))
                        .map(CSVFormatter::convertDataToCSVString)
                        .toList()
        );
        epicsList.keySet().stream()
                .map(i -> epicsList.get(i))
                .peek(epic -> dataToSave.add(CSVFormatter.convertDataToCSVString(epic)))
                .forEach(epic -> epic.getEpicsTasks().stream()
                        .forEach(subTask -> dataToSave.add(CSVFormatter.convertDataToCSVString(subTask, epic.code))));
    }

    public void save() {
        prepareDataToSave();
        String tmpName;
        Path pathToTmp = null;
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(tmpName = Files.createTempFile("Tmp4Manager", ".txt").toString()))) {
            pathToTmp = Path.of(tmpName);
            for (int i = 0; i < dataToSave.size(); i++) {
                bufferedWriter.write(dataToSave.get(i));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка сохранения данных в tmp-файл");
        }
        try {
            if (pathToTmp.toFile().exists()) {
                Files.copy(pathToTmp, fileName.toPath(), REPLACE_EXISTING);
                Files.delete(pathToTmp);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка записи данных в постоянный файл");
        }
    }

    public static void restoreTasks() {
        if (dataToSave.isEmpty())
            return;
        taskCounter = 0;
        subTaskCounter = 0;
        epicCounter = 0;
        for (int i = 1; i < dataToSave.size(); i++) {
            String str = dataToSave.get(i);
            int beginInd = str.indexOf(",") + 1;
            int endInd = str.indexOf(",", beginInd);
            TaskTypes type = TaskTypes.valueOf(str.substring(beginInd, endInd));
            switch (type) {
                case TASK: {
                    Task task = CSVFormatter.fromCSV2Task(str);
                    tasksList.put(task.code, task);
                    if (task.code > taskCounter)
                        taskCounter = task.code;
                    break;
                }
                case SUBTASK: {
                    int key = Integer.parseInt(str.substring(str.lastIndexOf(",") + 1));
                    if (epicsList.containsKey(key)) {
                        SubTask subTask = (SubTask) CSVFormatter.fromCSV2Task(str);
                        subTasksList.put(subTask.ownCode, subTask);
                        epicsList.get(key).epicsTasks.add(subTask);
                        if (subTask.ownCode > subTaskCounter)
                            subTaskCounter = subTask.ownCode;
                    } else
                        System.out.println("При чтении файла обнаружена ошибка сохранения эпиков и подзадач.");
                    break;
                }
                case EPIC: {
                    Epic epic = (Epic) CSVFormatter.fromCSV2Task(str);
                    epicsList.put(epic.code, epic);
                    if (epic.code > epicCounter)
                        epicCounter = epic.code;
                }
            }
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        //Выяснилось, что я прозевал сигнатуру метода. Переделал под требования ТЗ к спринту7
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        if (file.exists()) {
            try {
                fileBackedTaskManager.dataToSave = Files.readAllLines(file.toPath());
            } catch (IOException e) {
                throw new ManagerSaveException("Произошла ошибка чтения данных");
            }
        }
        fileBackedTaskManager.restoreTasks();
        return fileBackedTaskManager;
    }

    @Override
    public void createTask(String name, String description) {
        super.createTask(name, description);
        save();
    }

    @Override
    public Epic createEpic(String name, String descript, int subTasksNumber) {
        Epic epic = super.createEpic(name, descript, subTasksNumber);
        save();
        return epic;
    }

    @Override
    public void renewTask(Task task) {
        super.renewTask(task);
        save();
    }

    @Override
    public void makeTaskExecutable(Task task, int duration) {
        super.makeTaskExecutable(task, duration);
        save();
    }

    @Override
    public void removeTaskWithId(int taskId) {
        super.removeTaskWithId(taskId);
        save();
    }

    @Override
    public void changeSubTaskStatus(Integer epicId, Integer subNumber, Status newStat) {
        super.changeSubTaskStatus(epicId, subNumber, newStat);
        save();
    }

    @Override
    public void recountEpicStatus(Integer epicId) {
        super.recountEpicStatus(epicId);
        save();
    }

    @Override
    public boolean deleteEpicsSubTask(Integer epicId, Integer stNum) {
        boolean result = super.deleteEpicsSubTask(epicId, stNum);
        if (result)
            save();
        return result;
    }

    @Override
    public void deleteAllEpicsSubTask(Integer epicId) {
        super.deleteAllEpicsSubTask(epicId);
        save();
    }

    @Override
    public void addNewSubToEpic(Integer epicId, Integer taskId, Integer place) {
        super.addNewSubToEpic(epicId, taskId, place);
        save();
    }

    @Override
    public void deleteEpic(Integer epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

}