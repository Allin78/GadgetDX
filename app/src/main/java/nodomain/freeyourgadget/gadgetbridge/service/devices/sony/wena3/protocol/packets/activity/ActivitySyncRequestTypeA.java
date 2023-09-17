package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.activity;

import androidx.annotation.Nullable;

import java.util.Date;

public class ActivitySyncRequestTypeA extends ActivitySyncRequest {
    public ActivitySyncRequestTypeA(@Nullable Date stepCountLastSyncTime, @Nullable Date heartRateLastSyncTime, @Nullable Date behaviorLastSyncTime, @Nullable Date vo2MaxLastSyncTime) {
        super((byte) 0x1, stepCountLastSyncTime, heartRateLastSyncTime, behaviorLastSyncTime, vo2MaxLastSyncTime);
    }
}
