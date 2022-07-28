package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

/**
 * This abstract class is the common denominator for all data structures used inside commands and the corresponding responses.
 * @see Message
 */
public abstract class WithingsStructure {
    protected final static short HEADER_SIZE = 4;

    /**
     * Some messages have some end bytes, some have not.
     * Subclasses that need to have the eom appended need to overwrite this class and return true.
     * The default value is false.
     *
     * @return true if some end of message should be appended
     */
    public boolean withEndOfMessage() {
        return false;
    }

    public byte[] getRawData() {
        short length = (getLength());
        ByteBuffer rawDataBuffer = ByteBuffer.allocate(length);
        rawDataBuffer.putShort(getType());
        rawDataBuffer.putShort((short)(length - HEADER_SIZE));
        fillinTypeSpecificData(rawDataBuffer);
        return rawDataBuffer.array();
    }

    protected void addStringAsBytesWithLengthByte(ByteBuffer buffer, String str) {
        if (str == null) {
            buffer.put((byte)0);
        } else {
            byte[] stringAsBytes = str.getBytes(StandardCharsets.UTF_8);
            buffer.put((byte)stringAsBytes.length);
            buffer.put(stringAsBytes);
        }
    }

    protected void fillFromRawData(byte[] rawData) {
        fillFromRawDataAsBuffer(ByteBuffer.wrap(rawData));
    };

    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {};

    public abstract short getLength();

    protected abstract void fillinTypeSpecificData(ByteBuffer buffer);
    public abstract short getType();

    protected String getNextString(ByteBuffer byteBuffer) {
        // For strings in the raw data the first byte of the data is the length of the string:
        int stringLength = (short)(byteBuffer.get() & 255);
        byte[] stringBytes = new byte[stringLength];
        byteBuffer.get(stringBytes);
        return new String(stringBytes, Charset.forName("UTF-8"));
    }

    protected byte[] getNextByteArray(ByteBuffer byteBuffer) {
        int arrayLength = (short)(byteBuffer.get() & 255);
        byte[] nextByteArray = new byte[arrayLength];
        byteBuffer.get(nextByteArray);
        return nextByteArray;
    }

    protected void addByteArrayWithLengthByte(ByteBuffer buffer, byte[] data) {
        buffer.put((byte) data.length);
        if (data.length != 0) {
            buffer.put(data);
        }
    }
}
