package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class ActivitySampleTime extends WithingsStructure {

    private Date date;

    public Date getDate() {
        return date;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        long timestampInSeconds = rawDataBuffer.getInt() & 4294967295L;
        date = new Date(timestampInSeconds * 1000);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_TIME;
    }
}
