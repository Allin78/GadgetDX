package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.activity;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util.TimeUtil;

abstract class ActivitySyncRequest implements Wena3Packetable {
    public byte header;

    @Nullable
    public Date date1;
    @Nullable
    public Date date2;
    @Nullable
    public Date date3;
    @Nullable
    public Date date4;

    public ActivitySyncRequest(byte header, @Nullable Date date1, @Nullable Date date2, @Nullable Date date3, @Nullable Date date4) {
        this.header = header;
        this.date1 = date1;
        this.date2 = date2;
        this.date3 = date3;
        this.date4 = date4;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(17)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(header)
                .putInt(TimeUtil.dateToWenaTime((date1 == null ? (new Date(0)) : date1)))
                .putInt(TimeUtil.dateToWenaTime((date2 == null ? (new Date(0)) : date2)))
                .putInt(TimeUtil.dateToWenaTime((date3 == null ? (new Date(0)) : date3)))
                .putInt(TimeUtil.dateToWenaTime((date4 == null ? (new Date(0)) : date4)))
                .array();
    }
}

