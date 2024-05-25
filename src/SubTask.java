public class SubTask extends Task {
    // queueNumber?
    int codeInEpic;
    Status status;

    public SubTask(int code, String name, String description, int codeInEpic, Status status) {
        super(code, name, description);
        this.codeInEpic = codeInEpic;
        this.status = status;
    }

    public int getCodeInEpic() {
        return codeInEpic;
    }

    public void setCodeInEpic(int codeInEpic) {
        this.codeInEpic = codeInEpic;
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
                "codeInEpic=" + codeInEpic +
                ", status=" + status +
                '}' + super.toString();
    }
}
