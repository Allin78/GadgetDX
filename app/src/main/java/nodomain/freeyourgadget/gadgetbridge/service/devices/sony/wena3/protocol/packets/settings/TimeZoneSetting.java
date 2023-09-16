package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class TimeZoneSetting implements Wena3Packetable {
    public TimeZone timeZone;
    public Date referenceDate;

    public TimeZoneSetting(TimeZone tz, Date referenceDate) {
        this.timeZone = tz;
        this.referenceDate = referenceDate;
    }

    @Override
    public byte[] toByteArray() {
        int offset = timeZone.getOffset(referenceDate.getTime());
        return ByteBuffer
                .allocate(3)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte)0x11)
                .put((byte) (offset / 3_600_000))
                .put((byte) ((offset / 60_000) % 60))
                .array();
    }
}
