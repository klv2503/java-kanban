package ru.taskmanagment;

public class SubTask extends Task {
    // queueNumber?
    Status status;

    public SubTask(int code, String name, String description, Status status) {
        super(code, name, description);
        this.status = status;
    }

    public SubTask(Task task, Status status) {
        code = task.code;
        name = task.name;
        description = task.description;
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
                " status=" + status +
                "} " + super.toString();
    }
}
