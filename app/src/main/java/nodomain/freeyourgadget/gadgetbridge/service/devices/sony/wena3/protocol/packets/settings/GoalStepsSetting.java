package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class GoalStepsSetting implements Wena3Packetable {
    public boolean goalNotificationEnabled;
    public int goalSteps;

    public GoalStepsSetting(boolean goalNotificationEnabled, int goalSteps) {
        this.goalNotificationEnabled = goalNotificationEnabled;
        this.goalSteps = goalSteps;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(6)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x17)
                .put((byte) (goalNotificationEnabled ? 0x1 : 0x0))
                .putInt(goalSteps)
                .array();
    }
}
