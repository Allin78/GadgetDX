package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

public class WorkoutScreen extends WithingsStructure {
    @Override
    public short getType() {
        return WithingsStructureType.WORKOUT_ENTRY;
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
