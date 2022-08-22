package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class NotificationAttribute {
    private byte attributeID;
    private short attributeLength;
    private short attributeMaxLength;
    private String value;

    public void setAttributeMaxLength(short attributeMaxLength) {
        this.attributeMaxLength = attributeMaxLength;
    }

    public byte getAttributeID() {
        return attributeID;
    }

    public void setAttributeID(byte attributeID) {
        this.attributeID = attributeID;
    }

    public short getAttributeLength() {
        short length = (short)(value != null? value.getBytes(StandardCharsets.UTF_8).length : 0);
        if (attributeMaxLength > 0 && length >  attributeMaxLength) {
            length = attributeMaxLength;
        }

        return length;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public byte[] serialize() {
        attributeLength = getAttributeLength();
        int length = attributeLength + 3;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(attributeID);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(attributeLength);
        if (value != null) {
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.put(value.getBytes(StandardCharsets.UTF_8), 0, attributeLength);
        }

        return buffer.array();
    }

    public void deserialize(byte[] rawData) {
        ByteBuffer buffer = ByteBuffer.wrap(rawData);
        attributeID = buffer.get();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        attributeLength = buffer.getShort();
        buffer.order(ByteOrder.BIG_ENDIAN);
        if (attributeLength > 0) {
            byte[] rawValue = new byte[attributeLength];
            buffer.get(rawValue);
            value = new String(rawValue, StandardCharsets.UTF_8);
        }
    }
}
