package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util;

import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtil {
    public static int dateToWenaTime(Date date) {
        long epochStart = new GregorianCalendar(2020,0, 1, 0, 0, 0).getTimeInMillis();
        return (int) ((date.getTime() - epochStart) / (long)1000);
    }
}
