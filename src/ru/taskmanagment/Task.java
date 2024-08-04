package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Task {
    public int code;
    public String name;
    public String description;
    Status status;
    LocalDateTime startTime;
    Duration duration;

    public Task() {
        this.name = name;
        this.description = description;
    }

    public Task(int code, String name, String description) {
        this.code = code;
        this.description = description;
        this.name = name;
        status = Status.NEW;
    }

    public Task(int code, String name, Status status, String description,
                   String startTime, String duration) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.status = (status == null) ? Status.NEW : status;
        this.startTime = (startTime.isEmpty() || startTime.equals(" ")) ? null
                : LocalDateTime.parse(startTime, TimeManager.dateTimeFormatter);
        LocalTime localTime = (duration.isEmpty() || duration.equals(" ")) ? null
                : LocalTime.parse(duration, TimeManager.timeFormatter);
        this.duration = (localTime == null) ? Duration.ofMinutes(0)
                : Duration.ofMinutes(localTime.getHour() * 60 + localTime.getMinute());
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public Long getDurationInMinutes() {
        return duration.toMinutes();
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null)
            return null;
        else
            return startTime.plus(duration);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String str = " startTime= "
                + ((startTime == null) ? "не установлено;" : startTime.format(TimeManager.dateTimeFormatter))
                + " duration= "
                + ((duration == null) ? "не установлено" : TimeManager.duration2String(duration));
        return "Task{"
                + "code= " + code
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", status='" + status
                + str
                + '}';
    }

    public String taskToCSV() {
        String str = ((startTime == null) ? " ," : startTime.format(TimeManager.dateTimeFormatter) + ",")
                + ((duration == null) ? " ," : TimeManager.duration2String(duration) + ",")
                + ((startTime == null || duration == null) ? "," :
                getEndTime().format(TimeManager.dateTimeFormatter) + ",");
        return String.format("%d,TASK,%s,%s,%s,", code, name, status, description) + str;
    }
}
