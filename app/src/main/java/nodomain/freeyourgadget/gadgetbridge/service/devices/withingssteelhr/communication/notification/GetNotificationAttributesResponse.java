package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class GetNotificationAttributesResponse {
    private byte commandID = 0;
    private int notificationUID;
    private List<NotificationAttribute> attributes = new ArrayList<>();

    public GetNotificationAttributesResponse(int notificationUID) {
        this.notificationUID = notificationUID;
    }

    public void addAttribute(NotificationAttribute attribute) {
        attributes.add(attribute);
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getLength());
        buffer.put(commandID);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(notificationUID);
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (NotificationAttribute attribute : attributes) {
            buffer.put(attribute.serialize());
        }
        return buffer.array();
    }

    private int getLength() {
        int length = 5;
        for (NotificationAttribute attribute : attributes) {
            length += attribute.getAttributeLength() + 3;
        }

        return length;
    }
}
