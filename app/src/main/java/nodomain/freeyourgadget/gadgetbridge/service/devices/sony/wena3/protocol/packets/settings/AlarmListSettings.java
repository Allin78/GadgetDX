package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class AlarmListSettings implements Wena3Packetable {
    public static int MAX_ALARMS_IN_PACKET = 3;

    // While the Wena 3 has 9 alarms, the packet fits 3 at most
    // So to update all alarms you need to send 3 packets
    public List<SingleAlarmSetting> alarms;
    // Usually 0 for first packet, 3 for second, 6 for third
    public int alarmListOffset;

    public AlarmListSettings(List<SingleAlarmSetting> alarms, int alarmListOffset) {
        this.alarms = alarms;
        this.alarmListOffset = alarmListOffset;

        assert (this.alarmListOffset % 3) == 0;
        assert this.alarms.size() <= MAX_ALARMS_IN_PACKET;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(19).order(ByteOrder.LITTLE_ENDIAN);

        buf.put((byte) 0x06);

        for(int i = 0; i < MAX_ALARMS_IN_PACKET; i++) {
            buf.put((byte) (alarmListOffset + i));
            if(i < alarms.size()) {
                SingleAlarmSetting alarm = alarms.get(i);
                buf.put(alarm.toByteArray());
            } else {
                buf.put(SingleAlarmSetting.emptyPacket());
            }
        }

        return buf.array();
    }
}
