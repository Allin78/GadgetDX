package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class AlarmStatus extends WithingsStructure {

    private boolean enabled;

    public AlarmStatus(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public short getLength() {
        return 5;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(enabled? (byte) 1 : (byte) 0);
    }

    @Override
    public short getType() {
        return WithingsStructureType.ALARM_STATUS;
    }
}
