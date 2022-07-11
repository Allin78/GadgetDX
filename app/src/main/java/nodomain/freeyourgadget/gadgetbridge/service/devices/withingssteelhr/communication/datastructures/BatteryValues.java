package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.util.Arrays;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;

public class BatteryValues extends WithingsStructure {

    private short percent;
    private short status;
    private int volt;

    public short getPercent() {
        return percent;
    }

    public void setPercent(short percent) {
        this.percent = percent;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public int getVolt() {
        return volt;
    }

    public void setVolt(int volt) {
        this.volt = volt;
    }

    @Override
    public short getLength() {
        return 14;
    }

    @Override
    public void fillFromRawData(byte[] rawData) {
        if (rawData.length != BLETypeConversions.toInt16(rawData[1], rawData[0]) + 1 || rawData.length < 8) {
            throw new IllegalArgumentException();
        }

        percent = (short) BLETypeConversions.toUnsigned(rawData[2]);
        status = (short) BLETypeConversions.toUnsigned(rawData[3]);
        volt = BLETypeConversions.toUint32(rawData[7], rawData[6],rawData[5],rawData[4]);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
    }

    @Override
    short getType() {
        return WithingsStructureType.BATTERY_STATUS;
    }
}
