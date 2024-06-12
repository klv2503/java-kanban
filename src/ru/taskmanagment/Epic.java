package ru.taskmanagment;

import java.util.ArrayList;

public class Epic extends Task {
    Status epicStatus;
    ArrayList<SubTask> epicsTasks;

    public Epic(int code, String name, String description, Status epicStatus) {
        super(code, name, description);
        this.epicStatus = epicStatus;
        epicsTasks = new ArrayList<>();
    }

    public void setEpicStatus(Status epicStatus) {
        this.epicStatus = epicStatus;
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

    public ArrayList<SubTask> getEpicsTasks() {
        return epicsTasks;
    }

    public void setEpicsTasks(ArrayList<SubTask> epicsTasks) {
        this.epicsTasks = epicsTasks;
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

    public void addSubTaskInEndOfList(SubTask subTask){
        epicsTasks.add(subTask);
    }

    public void addSubTaskInEpic(SubTask subTaskForAdd, Integer place) {
        //Добавить можно подзадачу по индексу place.
        //Соответственно, пока предполагается, что place >= 0
        //если список подзадач пуст или короче, чем numInEpic, добавляем task в конец списка
        if (epicsTasks.isEmpty() || (epicsTasks.size() <= place)) {
            epicsTasks.add(subTaskForAdd);
        } else {
            epicsTasks.add(place, subTaskForAdd);
        }
    }

    public boolean deleteEpicsSubTaskByIndex(Integer subTaskNum) {
        boolean isSuccess = false;
        if (!epicsTasks.isEmpty() && (subTaskNum <= epicsTasks.size())) {
            isSuccess = (!(epicsTasks.remove(subTaskNum - 1) == null));
        }
        return isSuccess;
    }

    public void deleteAllEpicsSubTasksById(Integer subTaskId) {
        int index = 0;
        while (index >= 0) {
            index = seekFirstSubTaskByID(subTaskId);
            if ( index >= 0 ) {
                epicsTasks.remove(index);
            }
        }
    }

    public void deleteAllEpicsSubTasks() {
        epicsTasks = new ArrayList<>();
        epicStatus = Status.NEW;
    }

    public SubTask getSubTaskByIndex(Integer index) {
        if (index < 0 || epicsTasks.isEmpty() || (index >= epicsTasks.size())) {
            return null;
        } else {
            return epicsTasks.get(index);
        }
    }

    public int seekFirstSubTaskByID(Integer subTaskId) {
        //Если в списке нет подзадачи с subTaskId выдаем результат -1
        int resoult = -1;
        for (int i = 0; i < epicsTasks.size(); i++) {
            if (epicsTasks.get(i).code == subTaskId) {
                resoult = i;
                break;
            }
        }
        return resoult;
    }

    public ArrayList<Integer> seekSubTasksIndexesById(Integer subTaskId) {
        //В одном эпике может быть несколько экземпляров подзадачи subTaskId, выдаем список их индексов
        //Если нет ни одного экземпляра, возвращаем пустой список
        ArrayList<Integer> resoultArray = new ArrayList<>();
        for (int i = 0; i < epicsTasks.size(); i++) {
            if (epicsTasks.get(i).code == subTaskId) {
                resoultArray.add(i);
            }
        }
        return resoultArray;
    }

    public boolean changeSubTaskStatusByIndex(Integer index, Status newStatus) {
        boolean resoult = false;
        if (index >= epicsTasks.size()) {
            return resoult;
        }
        SubTask subTask = epicsTasks.get(index);
        if (subTask.getStatus().equals(newStatus)) {
            return resoult;
        } else {
            subTask.setStatus(newStatus);
            epicsTasks.set(index, subTask);
            resoult = true;
        }
        return resoult;
    }

    public void changeStringDataOfSubTask(SubTask subTask) {
        //Меняем наименование и описание у всех экземпляров subTask
        for (int i = 0; i < epicsTasks.size(); i++) {
            SubTask editedSubTask = epicsTasks.get(i);
            if (editedSubTask.code == subTask.code) {
                editedSubTask.name = subTask.name;
                editedSubTask.description = subTask.description;
                epicsTasks.set(i, editedSubTask);
            }
        }
    }

    public Status countEpicStatus() {
        if (epicsTasks.isEmpty()) {
            return Status.NEW;
        }
        epicStatus = Status.DONE;
        for (int i = 0; i < epicsTasks.size(); i++) {
            epicStatus = epicsTasks.get(i).getStatus();
            if (epicStatus.equals(Status.IN_PROGRESS)) {
                return Status.IN_PROGRESS;
            } else if (epicStatus.equals(Status.NEW)) {
                epicStatus = Status.NEW;
            }
        }
        return epicStatus;
    }
}