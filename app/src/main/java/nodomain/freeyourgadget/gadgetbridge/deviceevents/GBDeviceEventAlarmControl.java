package nodomain.freeyourgadget.gadgetbridge.deviceevents;

import androidx.annotation.NonNull;

import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.model.PhoneAlarmSpec;

public class GBDeviceEventAlarmControl extends GBDeviceEvent {
    public PhoneAlarmSpec.Action action;
    public int id;

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "id=" + id + ", action=" + action;
    }
}
