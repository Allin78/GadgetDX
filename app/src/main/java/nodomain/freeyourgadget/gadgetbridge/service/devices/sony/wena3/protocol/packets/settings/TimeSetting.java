package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util.TimeUtil;

public class TimeSetting implements Wena3Packetable {
    @Nullable public Date currentTime;

    public TimeSetting(Date dateTime) {
        this.currentTime = dateTime;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(5)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x10)
                .putInt(TimeUtil.dateToWenaTime(currentTime))
                .array();
    }
}
