package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util;

import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3Constants;

public class TimeUtil {
    public static int dateToWenaTime(Date date) {
        return (int) ((date.getTime() - SonyWena3Constants.EPOCH_START) / (long)1000);
    }
}
