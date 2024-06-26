package ru.taskmanagment;

public class SubTask extends Task {
    Status status;
    int ownCode;

    public SubTask(int code, String name, String description, int ownCode, Status status) {
        super(code, name, description);
        this.ownCode = ownCode;
        this.status = status;
    }

    public SubTask(Task task, int ownCode, Status status) {
        code = task.code;
        name = task.name;
        description = task.description;
        this.ownCode = ownCode;
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "ownCode= " + ownCode +
                " status=" + status +
                "} " + super.toString();
    }
}
