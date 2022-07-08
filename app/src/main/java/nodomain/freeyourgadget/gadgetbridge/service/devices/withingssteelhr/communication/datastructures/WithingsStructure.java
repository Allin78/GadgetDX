package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

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

    public void fillFromRawData(byte[] rawData) {};

    public abstract short getLength();

    protected abstract void fillinTypeSpecificData(ByteBuffer buffer);
    abstract short getType();
}
