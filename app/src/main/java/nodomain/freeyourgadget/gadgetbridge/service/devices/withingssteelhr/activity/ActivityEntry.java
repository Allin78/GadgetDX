package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity;

public class ActivityEntry {
    private int timestamp;
    private int duration;
    private int rawKind = -1;
    private int heartrate;
    private int steps;
    private int calories;
    private int distance;
    private int rawIntensity;

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(int heartrate) {
        this.heartrate = heartrate;
    }

    public int getRawKind() {
        return rawKind;
    }

    public void setRawKind(int rawKind) {
        this.rawKind = rawKind;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getRawIntensity() {
        return rawIntensity;
    }

    public void setRawIntensity(int rawIntensity) {
        this.rawIntensity = rawIntensity;
    }
}
