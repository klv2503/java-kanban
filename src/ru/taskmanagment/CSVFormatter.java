package ru.taskmanagment;

public class CSVFormatter {

    private CSVFormatter() {

    }

    public static String convertDataToCSVString(Task task) {
        return task.taskToCSV();
    }

    public static String convertDataToCSVString(Task task, int epicId) {
        String str = task.taskToCSV();
        str.replace("\n", "," + epicId + "\n");
        return str;
    }

    public static Task fromCSV2Task(String value) {
        String[] toTask = value.split(",");
        switch (toTask[1]) {
            case "TASK":
                return new Task(Integer.parseInt(toTask[0]), toTask[2], toTask[4]);
            case "EPIC":
                return new Epic(Integer.parseInt(toTask[0]), toTask[2], toTask[4], Status.valueOf(toTask[3]),
                        toTask[5], toTask[6], toTask[7]);
            case "SUBTASK":
                return new SubTask(Integer.parseInt(toTask[0]), toTask[2], Status.valueOf(toTask[3]), toTask[4],
                        toTask[5], toTask[6], Integer.parseInt(toTask[8]));
            default:
                return null;
        }
    }

}
