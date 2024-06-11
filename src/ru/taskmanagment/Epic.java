package ru.taskmanagment;

import java.util.ArrayList;

public class Epic extends Task {
    Status epicStatus;

    public Epic(int code, String name, String description, Status epicStatus) {
        super(code, name, description);
        this.epicStatus = epicStatus;
    }

    public void setEpicStatus(Status epicStatus) {
        this.epicStatus = epicStatus;
    }

    @Override
    public String toString() {
        return "Epic{ " +
                "epicCode= " + code +
                ", epicName= '" + name + '\'' +
                ", epicDescription= '" + description + '\'' +
                ", epicStatus= " + epicStatus +
                " } ";
    }

    public int getEpicCode() {
        return code;
    }

    public void setEpicCode(int code) {
        this.code = code;
    }

    public Status getEpicStatus() {
        return epicStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}