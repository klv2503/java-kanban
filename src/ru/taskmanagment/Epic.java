package ru.taskmanagment;

import java.util.ArrayList;

public class Epic extends Task implements TaskManager{
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
        return "Epic{" +
                "epicCode=" + code +
                ", epicName= " + name + '\'' +
                ", epicDescription= " + description + '\'' +
                ", epicStatus=" + epicStatus +
                "} ";
    }

    public int getEpicCode() {
        return code;
    }

    public void setEpicCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean isTaskExist(Integer code) {
        return false;
    }

    @Override
    public void createTask(String name, String description) {

    }

    @Override
    public Task getTaskWithId(int id) {
        return null;
    }

    @Override
    public void renewTask(Task task) {

    }

    @Override
    public void printTaskList() {

    }

    @Override
    public void removeTaskWithId(int id) {

    }

    @Override
    public void deleteAllTasks() {

    }

    @Override
    public boolean isSubTaskExist(Integer code) {
        return false;
    }

    @Override
    public boolean isEpicExist(Integer code) {
        return false;
    }

    @Override
    public void createEpic(String name, String description, int subTasksNumber) {

    }

    @Override
    public Epic getEpicByCode(Integer code) {
        return null;
    }

    @Override
    public void printEpicByCode(Integer code) {

    }

    @Override
    public void printEpicsTasks(Integer code) {

    }

    @Override
    public void addNewSubToEpic(Integer epicId, Integer taskId, Integer place) {

    }

    @Override
    public int getHowSubTasks(Integer code) {
        return 0;
    }

    @Override
    public SubTask getEpicsSubTaskByNumber(Integer epicCode, Integer subTaskNumber) {
        return null;
    }

    @Override
    public void countEpicStatus(Integer id) {

    }

    @Override
    public boolean deleteEpicsSubTask(Integer epicNum, Integer subTaskNum) {
        return false;
    }

    @Override
    public void deleteAllEpicsSubTask(Integer id) {

    }

    @Override
    public void deleteEpic(Integer id) {

    }

    @Override
    public void deleteAllEpics() {

    }

    @Override
    public void printEpicList() {

    }

    @Override
    public void deleteAllSubTasks() {

    }

    @Override
    public <T extends Task> boolean isListNotEmpty(ArrayList<T> currentArray) {
        return false;
    }

    @Override
    public <T extends Task> void addEpicToEpic(ArrayList<T> currentList, Integer dependEpic) {

    }
}
