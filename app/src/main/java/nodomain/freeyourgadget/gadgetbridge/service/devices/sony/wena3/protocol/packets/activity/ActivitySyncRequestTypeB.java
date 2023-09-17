package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.activity;

import androidx.annotation.Nullable;

import java.util.Date;

public class ActivitySyncRequestTypeB extends ActivitySyncRequest {
    public ActivitySyncRequestTypeB(@Nullable Date stressLastSyncTime, @Nullable Date bodyEnergyLastSyncTime, @Nullable Date caloriesLastSyncTime, @Nullable Date eventsLastSyncTime) {
        super((byte) 0x2, stressLastSyncTime, bodyEnergyLastSyncTime, caloriesLastSyncTime, eventsLastSyncTime);
    }
}
