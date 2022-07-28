package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

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
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        percent = (short)(rawDataBuffer.get() & 255);
        status = (short)(rawDataBuffer.get() & 255);
        volt = rawDataBuffer.getInt();
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
    }

    @Override
    public short getType() {
        return WithingsStructureType.BATTERY_STATUS;
    }
}
