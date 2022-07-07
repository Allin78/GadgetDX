package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

/**
 * This abstract class is the common denominator for all data structures used inside commands and the corresponding responses.
 * @see Message
 */
public abstract class WithingsStructure {
    final static short HEADER_SIZE = 4;
    public byte[] getRawData() {
        ByteBuffer rawDataBuffer = ByteBuffer.allocate(getLength());
        rawDataBuffer.putShort(getType());
        rawDataBuffer.putShort((short)(getLength() - HEADER_SIZE));
        fillinTypeSpecificData(rawDataBuffer);
        return rawDataBuffer.array();
    }

    public void fillFromRawData(byte[] rawData) {};

    public abstract short getLength();
    protected abstract void fillinTypeSpecificData(ByteBuffer buffer);
    abstract short getType();
}
