package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class SourceAppId extends WithingsStructure {

    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public short getLength() {
        return (short) ((appId != null ? appId.getBytes().length : 0) + 5);
    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        appId = getNextString(rawDataBuffer);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        addStringAsBytesWithLengthByte(buffer, appId);
    }

    @Override
    public short getType() {
        return WithingsStructureType.NOTIFICATION_APP_ID;
    }
}
