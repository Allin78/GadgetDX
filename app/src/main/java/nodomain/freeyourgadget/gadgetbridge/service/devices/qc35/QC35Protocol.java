package nodomain.freeyourgadget.gadgetbridge.service.devices.qc35;

import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsFragment;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class QC35Protocol extends GBDeviceProtocol {
    Logger logger = LoggerFactory.getLogger(getClass());
    protected QC35Protocol(GBDevice device) {
        super(device);
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        logger.debug("response: {}", StringUtils.bytesToHex(responseData));

        return null;
    }

    @Override
    public byte[] encodeSendConfiguration(String config) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());

        if(config.equals(DeviceSettingsPreferenceConst.PREF_QC35_NOISE_CANCELLING_LEVEL)){
            int level = prefs.getInt(config, 0);
            if(level == 2){
                level = 1;
            }else if(level == 1){
                level = 3;
            }
            return new byte[]{0x01, 0x06, 0x02, 0x01, (byte) level};
        }

        return null;
    }
}
