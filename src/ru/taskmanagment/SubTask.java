package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SubTask extends Task {
    Status status;
    int ownCode;
    LocalDateTime startTime;
    Duration duration;


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
                   String stTime, String stDuration, int code) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.ownCode = ownCode;
        this.status = status;
        this.startTime = (stTime.isEmpty()) ? null : LocalDateTime.parse(stTime, TimeManager.dateTimeFormatter);
        LocalTime localTime = (stDuration.isEmpty()) ? null : LocalTime.parse(stDuration, TimeManager.timeFormatter);
        this.duration = (localTime == null) ? Duration.ofMinutes(0)
                : Duration.ofMinutes(localTime.getHour() * 60 + localTime.getMinute());
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getOwnCode() {
        return ownCode;
    }

    public Duration getDuration() {
        return duration;
    }

    public Long getDurationInMinutes() {
        return duration.toMinutes();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime anyDateTime) {
        this.startTime = anyDateTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null)
            return null;
        if (duration == null)
            return startTime;
        else
            return startTime.plus(duration);
    }

    @Override
    public String toString() {
        String str = " startTime= "
                + ((startTime == null) ? "не установлено" : startTime.format(TimeManager.dateTimeFormatter))
                + " duration= "
                + ((duration == null) ? "не установлено" : TimeManager.duration2String(duration));
        return "SubTask{" +
                "ownCode= " + ownCode + " status=" + status + " "
                + super.toString()
                + str;
    }

    public String taskToCSV() {
        String str = ((startTime == null) ? "," : startTime.format(TimeManager.dateTimeFormatter) + ",")
                + ((duration == null) ? "," : TimeManager.duration2String(duration) + ",")
                + ((startTime == null || duration == null) ? "," :
                getEndTime().format(TimeManager.dateTimeFormatter) + ",");
        return String.format("%d,SUBTASK,%s,%s,%s,", ownCode, name, status, description)
                + str
                + code + "\n"; //String.format("%d%n", code);
    }
}
