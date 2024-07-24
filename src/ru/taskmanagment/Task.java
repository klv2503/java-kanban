package ru.taskmanagment;

public class Task {
    public int code;
    public String name;
    public String description;

    public Task() {
        this.name = name;
        this.description = description;
    }

    public Task(int code, String name, String description) {
        this.code = code;
        this.description = description;
        this.name = name;
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

    @Override
    public String toString() {
        return "Task{" +
                "code= " + code +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String taskToCSV() {
        return String.format("%d,TASK,%s, ,%s%n", code, name, description);
    }
}
