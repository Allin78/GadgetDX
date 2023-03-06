package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GetNotificationAttributes {
    private byte commandID;
    private int notificationUID;
    private List<RequestedNotificationAttribute> attributes = new ArrayList<>();

    public byte getCommandID() {
        return commandID;
    }

    public void setCommandID(byte commandID) {
        this.commandID = commandID;
    }

    public int getNotificationUID() {
        return notificationUID;
    }

    public void setNotificationUID(int notificationUID) {
        this.notificationUID = notificationUID;
    }

    public List<RequestedNotificationAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public void addAttribute(RequestedNotificationAttribute attribute) {
        attributes.add(attribute);
    }

    public void deserialize(byte[] rawData) {
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        commandID = buffer.get();
        notificationUID = buffer.getInt();
        while (buffer.hasRemaining()) {
            RequestedNotificationAttribute requestedNotificationAttribute = new RequestedNotificationAttribute();
            int length = 1;
            if (buffer.remaining() >= 3) {
                length = 3;
            }

            byte[] rawAttributeData = new byte[length];
            buffer.get(rawAttributeData);
            requestedNotificationAttribute.deserialize(rawAttributeData);
            attributes.add(requestedNotificationAttribute);
        }
    }

    public byte[] serialize() {
        return new byte[0];
    }
}
