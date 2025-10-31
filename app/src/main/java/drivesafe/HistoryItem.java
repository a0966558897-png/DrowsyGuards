package drivesafe;

public class HistoryItem {
    public String time;
    public int score;
    public String status;

    public HistoryItem(String time, int score, String status) {
        this.time = time;
        this.score = score;
        this.status = status;
    }
}
