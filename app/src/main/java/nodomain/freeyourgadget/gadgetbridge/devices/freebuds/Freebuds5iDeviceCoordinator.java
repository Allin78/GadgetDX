package nodomain.freeyourgadget.gadgetbridge.devices.freebuds;


import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;

public class Freebuds5iDeviceCoordinator extends AbstractFreebudsCoordinator {

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("huawei freebuds 5i.*", Pattern.CASE_INSENSITIVE);
    }

    //TODO: How to find the best value for BONDING_STYLE_????

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_freebuds_5i;
    }
}
