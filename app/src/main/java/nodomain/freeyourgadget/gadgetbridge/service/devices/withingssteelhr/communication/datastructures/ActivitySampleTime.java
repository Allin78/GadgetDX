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
    public void fillFromRawData(byte[] rawData) {
        if (rawData.length < 4) {
            throw new IllegalArgumentException();
        }

        long timestampInSeconds = BLETypeConversions.toUint32(rawData[3], rawData[2], rawData[1], rawData[0]);
        date = new Date(timestampInSeconds * 1000);
    }

    @Override
    short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_TIME;
    }
}
