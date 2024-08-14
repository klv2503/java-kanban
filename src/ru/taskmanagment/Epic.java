package ru.taskmanagment;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;

public class Epic extends Task {
    LocalDateTime endTime;
    ArrayList<SubTask> epicsTasks;

    public Epic(int code, String name, String description, Status status) {
        super(code, name, description);
        this.status = status;
        epicsTasks = new ArrayList<>();
    }

    public Epic(int code, String name, Status status, String description,
                String stTime, String strDuration, String eTime) {
        super(code, name, description);
        this.status = status;
        epicsTasks = new ArrayList<>();
        this.startTime = (stTime.isEmpty()) ? null : LocalDateTime.parse(stTime, TimeManager.dateTimeFormatter);
        this.endTime = (eTime.equals(" ")) ? null : LocalDateTime.parse(eTime, TimeManager.dateTimeFormatter);
        LocalTime localTime = LocalTime.parse(strDuration, TimeManager.timeFormatter);
        this.duration = Duration.ofMinutes(localTime.getHour() * 60 + localTime.getMinute());
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ArrayList<SubTask> getEpicsTasks() {
        return epicsTasks;
    }

    public void setEpicsTasks(ArrayList<SubTask> epicsTasks) {
        this.epicsTasks = epicsTasks;
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
                .map(SubTask::getEndTime)
                .max((LocalDateTime t1, LocalDateTime t2) -> (t1.isBefore(t2)) ? -1 : 1);
        this.endTime = optTime.orElse(null);
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
                + ", status= " + status
                + str
                + " } ";
    }

    public String taskToCSV() {
        String str = ((startTime == null) ? "," : startTime.format(TimeManager.dateTimeFormatter) + ",")
                + ((duration == null) ? "," : TimeManager.duration2String(duration) + ",")
                + ((endTime == null) ? " " : endTime.format(TimeManager.dateTimeFormatter));
        return String.format("%d,EPIC,%s,%s,%s,%s", code, name, status, description, str);
    }

    public void addSubTaskInEpic(SubTask subTaskForAdd) {
        epicsTasks.add(subTaskForAdd);
        recountEpicData();
    }

    public boolean deleteEpicsSubTask(SubTask subTask) {
        boolean isSuccess = epicsTasks.remove(subTask);
        recountEpicData();
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
        status = Status.NEW;
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
        setStatus(Status.IN_PROGRESS);
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
            setStatus(Status.NEW);
        if (countDone == epicsTasks.size())
            setStatus(Status.DONE);
        return getStatus();
    }

    public void recountEpicData() {
        //При изменениях в списке подзадач
        setStartTime();
        setEndTime();
        setDuration();
        countEpicStatus();
    }
}