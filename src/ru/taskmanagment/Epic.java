package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;


public class Epic extends Task {
    Status epicStatus;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Duration duration;
    ArrayList<SubTask> epicsTasks;

    public Epic(int code, String name, String description, Status epicStatus) {
        super(code, name, description);
        this.epicStatus = epicStatus;
        epicsTasks = new ArrayList<>();
    }

    public Epic(int code, String name, String description, Status epicStatus,
                String stTime, String strDuration, String eTime) {
        super(code, name, description);
        this.epicStatus = epicStatus;
        epicsTasks = new ArrayList<>();
        this.startTime = (stTime.isEmpty()) ? null : LocalDateTime.parse(stTime, TimeManager.dateTimeFormatter);
        this.endTime = (eTime.equals(" ")) ? null : LocalDateTime.parse(eTime, TimeManager.dateTimeFormatter);
        LocalTime localTime = LocalTime.parse(strDuration, TimeManager.timeFormatter);
        this.duration = Duration.ofMinutes(localTime.getHour() * 60 + localTime.getMinute());
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime() {
        if (epicsTasks.isEmpty())
            this.startTime = null;
        else
            this.startTime = epicsTasks.getFirst().startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        Optional<LocalDateTime> optTime = epicsTasks.stream()
                .map(s -> s.getEndTime())
                .max((LocalDateTime t1, LocalDateTime t2) -> (t1.isBefore(t2)) ? -1 : 1);
        if (optTime.isPresent())
            this.endTime = optTime.get();
        else
            this.endTime = null;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration() {
        this.duration = Duration.ofMinutes(
                epicsTasks.stream()
                        .map(SubTask::getDuration)
                        .map(Duration::toMinutes)
                        .reduce(0L, Long::sum)
        );
    }

    @Override
    public String toString() {
        String str = ", startTime="
                + ((startTime == null) ? "не установлено " : startTime.format(TimeManager.dateTimeFormatter))
                + ", duration="
                + ((duration == null) ? "не установлено " : TimeManager.duration2String(duration))
                + ", endTime="
                + ((endTime == null) ? "не установлено " : endTime.format(TimeManager.dateTimeFormatter));
        return "Epic{ " +
                "epicCode= " + code
                + ", epicName= '" + name + '\''
                + ", epicDescription= '" + description + '\''
                + ", epicStatus= " + epicStatus
                + str
                + " } ";
    }

    public String taskToCSV() {
        String str = ((startTime == null) ? "," : startTime.format(TimeManager.dateTimeFormatter) + ",")
                + ((duration == null) ? "," : TimeManager.duration2String(duration) + ",")
                + ((endTime == null) ? " " : endTime.format(TimeManager.dateTimeFormatter));
        return String.format("%d,EPIC,%s,%s,%s,%s%n", code, name, epicStatus, description, str);
    }

    public void addSubTaskInEndOfList(SubTask subTask) {
        epicsTasks.add(subTask);
        setStartTime();
        setEndTime();
        setDuration();
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
        setStartTime();
        setEndTime();
        setDuration();
    }

    public boolean deleteEpicsSubTaskByIndex(Integer subTaskNum) {
        boolean isSuccess = false;
        if (!epicsTasks.isEmpty() && (subTaskNum <= epicsTasks.size())) {
            isSuccess = (!(epicsTasks.remove(subTaskNum - 1) == null));
            if (isSuccess) {
                setStartTime();
                setEndTime();
                setDuration();
            }
        }
        return isSuccess;
    }

    public void deleteAllEpicsSubTasksByTaskId(Integer taskId) {
        int index = 0;
        while (index >= 0) {
            index = seekFirstSubTaskByID(taskId);
            if (index >= 0) {
                epicsTasks.remove(index);
            }
        }
        setStartTime();
        setEndTime();
        setDuration();
    }

    public void clearEpicsSubTasks() {
        epicsTasks = new ArrayList<>();
        epicStatus = Status.NEW;
        setStartTime();
        setEndTime();
        setDuration();
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
        int result = -1;
        for (int i = 0; i < epicsTasks.size(); i++) {
            if (epicsTasks.get(i).code == subTaskId) {
                result = i;
                break;
            }
        }
        return result;
    }

    public ArrayList<Integer> seekSubTasksIndexesByTaskId(Integer taskId) {
        //В одном эпике может быть несколько экземпляров подзадач, сгенерированных на основе задачи Task,
        // выдаем список их индексов. Если нет ни одного экземпляра, возвращаем пустой список
        ArrayList<Integer> resultArray = new ArrayList<>();
        for (int i = 0; i < epicsTasks.size(); i++) {
            if (epicsTasks.get(i).code == taskId) {
                resultArray.add(i);
            }
        }
        return resultArray;
    }

    public boolean changeSubTaskStatusByIndex(Integer index, Status newStatus) {
        boolean result = false;
        if (index >= epicsTasks.size()) {
            return result;
        }
        SubTask subTask = epicsTasks.get(index);
        if (subTask.getStatus().equals(newStatus)) {
            return result;
        } else {
            subTask.setStatus(newStatus);
            epicsTasks.set(index, subTask);
            result = true;
        }
        return result;
    }

    public void changeStringDataOfSubTask(Task task) {
        //Меняем наименование и описание у всех экземпляров subTask
        for (int i = 0; i < epicsTasks.size(); i++) {
            SubTask editedSubTask = epicsTasks.get(i);
            if (editedSubTask.code == task.code) {
                editedSubTask.name = task.name;
                editedSubTask.description = task.description;
                epicsTasks.set(i, editedSubTask);
            }
        }
    }

    public Status countEpicStatus() {
        if (epicsTasks.isEmpty()) {
            return Status.NEW;
        }
        setEpicStatus(Status.IN_PROGRESS);
        int countNew = 0;
        int countInProgress = 0;
        int countDone = 0;
        for (int i = 0; i < epicsTasks.size(); i++) {
            switch (epicsTasks.get(i).getStatus()) {
                case NEW -> countNew++;
                case IN_PROGRESS -> countInProgress++;
                case DONE -> countDone++;
            }
        }
        if (countNew == epicsTasks.size())
            setEpicStatus(Status.NEW);
        if (countDone == epicsTasks.size())
            setEpicStatus(Status.DONE);
        return getEpicStatus();
    }

}