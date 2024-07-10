package ru.taskmanagment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileBackedTaskManager extends InMemoryTaskManager {

    static Path fileName;
    final String home = System.getProperty("user.home");
    //Пока список тупо перезаписывается при вызове метода save.
    //Скорее всего, список переделаю в двусвязную мапу по образцу хранения истории.
    static List<String> dataToSave;

    public FileBackedTaskManager(String fileToSave) {
        fileName = Paths.get(home, fileToSave);
        dataToSave = new ArrayList<>();
    }

    public String convertDataToString(Task task) {
        //Метод не знает, к какому эпику относится подзадача, его номер к строке подзадачи добавляется
        //в prepareDataToSave(). Из-за этого добавление "\n" (для всех - из соображений общности)
        //тоже оставлено в prepareDataToSave()
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            return String.format("%d,EPIC,%s,%s,%s", epic.code, epic.name, epic.epicStatus, epic.description);
        } else if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d", subTask.ownCode, subTask.name,
                    subTask.status, subTask.description, subTask.code);
        } else {
            return String.format("%d,TASK,%s, ,%s", task.code, task.name, task.description);
        }
    }

    public void prepareDataToSave() {
        //В состоянии менеджера первая строка запоминает значения счетчиков. Они требуются для запуска менеджера
        //с сохраненной точки и удобнее их записать в файл, чем при чтении файла высчитывать.
        //Далее строки идут в формате id,type,name,status,description,parentTask,epic.
        //Сам формат в файл не пишется. Поскольку в принятой схеме связь SubTask и Task идет через поле SubTask.code,
        //в формат добавлено поле parentTask для восстановления этой связи при загрузке из файла
        dataToSave.clear();
        dataToSave.add(String.format("%d,%d,%d%n", taskCounter, subTaskCounter, epicCounter));
        for (int i : tasksList.keySet())
            dataToSave.add(convertDataToString(tasksList.get(i)) + "\n");
        for (int i : epicsList.keySet()) {
            dataToSave.add(convertDataToString(epicsList.get(i)) + "\n");
            for (int j = 0; j < epicsList.get(i).epicsTasks.size(); j++) {
                dataToSave.add(convertDataToString(epicsList.get(i).epicsTasks.get(j)) + "," + i + "\n");
            }
        }
    }

    public void save() {
        //Методу возвращено название save, теперь он снова пишет файл при каждом изменении в списках объектов
        prepareDataToSave();
        String tmpName;
        Path pathToTmp = null;
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(tmpName = Files.createTempFile("Tmp4Manager", ".txt").toString()))) {
            pathToTmp = Path.of(tmpName);
            for (int i = 0; i < dataToSave.size(); i++) {
                bufferedWriter.write(dataToSave.get(i));
            }
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка сохранения данных в tmp-файл");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
        try {
            if (pathToTmp.toFile().exists()) {
                Files.copy(pathToTmp, fileName, REPLACE_EXISTING);
                Files.delete(pathToTmp);
            }
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка записи данных в постоянный файл");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static Task fromString(String value) {
        String[] toTask = value.split(",");
        switch (toTask[1]) {
            case "TASK":
                return new Task(Integer.parseInt(toTask[0]), toTask[2], toTask[4]);
            case "EPIC":
                return new Epic(Integer.parseInt(toTask[0]), toTask[2], toTask[4], Status.valueOf(toTask[3]));
            case "SUBTASK":
                return new SubTask(Integer.parseInt(toTask[5]), toTask[2], toTask[4],
                        Integer.parseInt(toTask[0]), Status.valueOf(toTask[3]));
            default:
                return null;
        }
    }

    public static void restoreTasks() {
        if (dataToSave.isEmpty())
            return;
        String[] workStr = dataToSave.get(0).split(",");
        taskCounter = Integer.parseInt(workStr[0]);
        subTaskCounter = Integer.parseInt(workStr[1]);
        epicCounter = Integer.parseInt(workStr[2]);
        for (int i = 1; i < dataToSave.size(); i++) {
            String str = dataToSave.get(i);
            int beginInd = str.indexOf(",") + 1;
            int endInd = str.indexOf(",", beginInd);
            TaskTypes type = TaskTypes.valueOf(str.substring(beginInd, endInd));
            switch (type) {
                case TASK: {
                    Task task = fromString(str);
                    tasksList.put(task.code, task);
                    break;
                }
                case SUBTASK: {
                    int key = Integer.parseInt(str.substring(str.lastIndexOf(",") + 1));
                    if (epicsList.containsKey(key)) {
                        SubTask subTask = (SubTask) fromString(str);
                        subTasksList.put(subTask.ownCode, subTask);
                        epicsList.get(key).epicsTasks.add(subTask);
                    } else
                        System.out.println("При чтении файла обнаружена ошибка сохранения эпиков и подзадач.");
                    break;
                }
                case EPIC: {
                    Epic epic = (Epic) fromString(str);
                    epicsList.put(epic.code, epic);
                }
            }
        }
    }

    public static void loadFromFile(File file) {
        if (!file.exists())
            return;
        try {
            dataToSave = Files.readAllLines(file.toPath());
            restoreTasks();
        } catch (Exception e) {
            try {
                throw new ManagerSaveException("Произошла ошибка чтения данных");
            } catch (ManagerSaveException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void createTask(String name, String description) {
        super.createTask(name, description);
        save();
    }

    @Override
    public void createEpic(String name, String descript, int subTasksNumber) {
        super.createEpic(name, descript, subTasksNumber);
        save();
    }

    @Override
    public void renewTask(Task task) {
        super.renewTask(task);
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
