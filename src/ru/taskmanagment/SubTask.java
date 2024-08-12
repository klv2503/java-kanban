package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    int ownCode;

    public SubTask(int code, String name, String description, int ownCode, Status status) {
        super(code, name, description);
        this.ownCode = ownCode;
        this.status = status;
    }

    public SubTask(Task task, int ownCode, Status status, int duration, LocalDateTime startTime) {
        this.code = task.code;
        this.name = task.name;
        this.description = task.description;
        this.ownCode = ownCode;
        this.status = status;
        this.startTime = startTime;
        this.duration = Duration.ofMinutes(duration);
    }

    public SubTask(int ownCode, String name, Status status, String description,
                   String startTime, String duration, int code) {
        super(code, name, status, description, startTime, duration);
        this.ownCode = ownCode;
    }

    public int getOwnCode() {
        return ownCode;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "ownCode= " + ownCode + " "
                + super.toString();
    }

    public String taskToCSV() {
        String str = ((startTime == null) ? "," : startTime.format(TimeManager.dateTimeFormatter) + ",")
                + ((duration == null) ? "," : TimeManager.duration2String(duration) + ",")
                + ((startTime == null || duration == null) ? "," :
                getEndTime().format(TimeManager.dateTimeFormatter) + ",");
        return String.format("%d,SUBTASK,%s,%s,%s,", ownCode, name, status, description)
                + str
                + code;
    }
}