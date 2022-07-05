package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

public class BatteryRequest extends WithingsStructure {
    @Override
    public short getType() {
        return WithingsStructureType.BATTERY_STATUS;
    }

    @Override
    public void addSubStructure(WithingsStructure subStructure) {

    }

    @Override
    public short getLength() {
        return 0;
    }

    @Override
    public byte[] getRawData() {
        return new byte[0];
    }
}
