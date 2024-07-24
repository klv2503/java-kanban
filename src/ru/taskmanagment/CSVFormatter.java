package ru.taskmanagment;

public class CSVFormatter {

    private CSVFormatter() {

    }

    //Как мне объяснили, поиск "\n" под разными операционными средами может давать разные результаты,
    //а раз добавление CR для SubTask выносится в метод конвертации, для остальных объектов сделал так же
    public static String convertDataToCSVString(Task task) {
        return task.taskToCSV() + "\n";
    }

    public static String convertDataToCSVString(Task task, int epicId) {
        return task.taskToCSV() + "," + epicId + "\n";
    }

    public static Task fromCSV2Task(String value) {
        String[] toTask = value.split(",");
        //System.out.println("0 = " + toTask[0] + "length = " + toTask.length);
        return switch (toTask[1]) {
            case "TASK" -> new Task(Integer.parseInt(toTask[0]), toTask[2], Status.valueOf(toTask[3]), toTask[4],
                    toTask[5], toTask[6]);
            case "EPIC" -> new Epic(Integer.parseInt(toTask[0]), toTask[2], Status.valueOf(toTask[3]), toTask[4],
                    toTask[5], toTask[6], toTask[7]);
            case "SUBTASK" -> new SubTask(Integer.parseInt(toTask[0]), toTask[2], Status.valueOf(toTask[3]), toTask[4],
                    toTask[5], toTask[6], Integer.parseInt(toTask[8]));
            default -> null;
        };
    }

}