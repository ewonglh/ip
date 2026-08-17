package data;

public class Task {
    private String task;
    private boolean done;

    public Task(String task) {
        this.task = task;
        this.done = false;
    }

    public void markAsDone() {
        this.done = true;
    }

    public void markAsNotDone() {
        this.done = false;
    }

    @Override
    public String toString() {
        return String.format("[%c] " + task, (done ? 'X' : ' '));
    }

    public boolean isDone() {
        return done;
    }
}