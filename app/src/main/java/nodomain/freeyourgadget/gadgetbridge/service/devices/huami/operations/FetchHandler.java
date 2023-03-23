package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.operations;

import java.util.GregorianCalendar;

public interface FetchHandler {


    /**
     * Handle the buffered activity data.
     *
     * @param timestamp The timestamp of the first sample. This function should update this to the
     *                  timestamp of the last processed sample.
     * @param bytes     the buffered bytes
     * @return true on success
     */
    boolean handleActivityData(final GregorianCalendar timestamp, final byte[] bytes);

    String getLastSyncTimeKey();

    byte getCommandDataType();

    public String getDataName();

    int getDataType();
}
