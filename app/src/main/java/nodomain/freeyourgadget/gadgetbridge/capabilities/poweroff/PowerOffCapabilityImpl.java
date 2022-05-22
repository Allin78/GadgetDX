package nodomain.freeyourgadget.gadgetbridge.capabilities.poweroff;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsHandler;
import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapability;
import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapabilityImpl;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class PowerOffCapabilityImpl extends AbstractCapabilityImpl {
    public static List<Integer> getActions() {
        return Collections.singletonList(R.layout.device_item_info_poweroff);
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(final GBDevice device) {
        return new int[0];
    }

    @Override
    public void registerPreferences(final Context context, final AbstractCapability capability, final DeviceSpecificSettingsHandler handler) {
        // Nothing to do
    }

    @Override
    public void configureCardShortcuts(final ViewGroup infos, final GBDevice device) {
        final ImageView powerOff = infos.findViewById(R.id.device_action_power_off);

        if (powerOff == null) {
            return;
        }

        powerOff.setVisibility(View.GONE);
        if (device.isInitialized()) {
            powerOff.setVisibility(View.VISIBLE);
            powerOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(infos.getContext())
                            .setTitle(R.string.controlcenter_power_off_confirm_title)
                            .setMessage(R.string.controlcenter_power_off_confirm_description)
                            .setIcon(R.drawable.ic_power_settings_new)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int whichButton) {
                                    GBApplication.deviceService().onPowerOff();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            });
        }
    }
}
