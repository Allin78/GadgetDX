package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RequestedNotificationAttribute {
    private byte attributeID;
    private short attributeMaxLength;

    public byte getAttributeID() {
        return attributeID;
    }

    public void setAttributeID(byte attributeID) {
        this.attributeID = attributeID;
    }

    public short getAttributeMaxLength() {
        return attributeMaxLength;
    }

    public void setAttributeMaxLength(short attributeMaxLength) {
        this.attributeMaxLength = attributeMaxLength;
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(attributeID);
        buffer.putShort(attributeMaxLength);
        return buffer.array();
    }

    public void deserialize(byte[] rawData) {
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        attributeID = buffer.get();
        if (buffer.capacity() >= 3) {
            attributeMaxLength = buffer.getShort();
        }
    }
}
