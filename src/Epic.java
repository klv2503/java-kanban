public class Epic {
    int epicCode;
    String epicName;
    Status epicStatus;

    public Epic(String epicName, Status epicStatus) {
        this.epicName = epicName;
        this.epicStatus = epicStatus;
    }

    public void setEpicStatus(Status epicStatus) {
        this.epicStatus = epicStatus;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "epicCode=" + epicCode +
                ", epicName='" + epicName + '\'' +
                ", epicStatus=" + epicStatus +
                '}';
    }

    public int getEpicCode() {
        return epicCode;
    }

    public void setEpicCode(int epicCode) {
        this.epicCode = epicCode;
    }

    public String getEpicName() {
        return epicName;
    }

    public void setEpicName(String epicName) {
        this.epicName = epicName;
    }
}
