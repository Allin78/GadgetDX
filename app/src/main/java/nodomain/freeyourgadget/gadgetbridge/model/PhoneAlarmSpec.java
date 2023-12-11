package nodomain.freeyourgadget.gadgetbridge.model;

public class PhoneAlarmSpec {
    public enum Action {
        ALERT,
        DISMISS,
        SNOOZE,
    }

    public int id;
    public Action action;
    public int timestamp;
    public String name;
}
