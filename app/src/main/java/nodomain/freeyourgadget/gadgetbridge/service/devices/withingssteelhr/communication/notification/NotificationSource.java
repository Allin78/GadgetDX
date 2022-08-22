package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Random;

public class NotificationSource {
    private byte eventID;
    private byte eventFlags;
    private byte categoryId;
    private byte categoryCount;
    private int notificationUID;

    public NotificationSource(int notificationUID, byte eventID, byte eventFlags, byte categoryId, byte categoryCount) {
        this.eventID = eventID;
        this.eventFlags = eventFlags;
        this.categoryId = categoryId;
        this.categoryCount = categoryCount;
        this.notificationUID = Integer.valueOf(new Random().nextInt());
    }

    public int getNotificationUID() {
        return notificationUID;
    }

    void setNotificationUID(int notificationUID) {
        this.notificationUID = notificationUID;
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(eventID);
        buffer.put(eventFlags);
        buffer.put(categoryId);
        buffer.put(categoryCount);
        buffer.putInt(notificationUID);
        return buffer.array();
    }
}
