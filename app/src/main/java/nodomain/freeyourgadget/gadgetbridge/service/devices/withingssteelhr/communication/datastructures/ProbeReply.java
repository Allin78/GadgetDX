package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class ProbeReply extends WithingsStructure {

    private int yetUnknown1;
    private String name;
    private String mac;
    private String secret;
    private int yetUnknown2;
    private String mId;
    private int yetUnknown3;
    private int firmwareVersion;
    private int yetUnknown4;

    public int getYetUnknown1() {
        return yetUnknown1;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }

    public String getSecret() {
        return secret;
    }

    public int getYetUnknown2() {
        return yetUnknown2;
    }

    public String getmId() {
        return mId;
    }

    public int getYetUnknown3() {
        return yetUnknown3;
    }

    public int getFirmwareVersion() {
        return firmwareVersion;
    }

    public int getYetUnknown4() {
        return yetUnknown4;
    }

    @Override
    public short getLength() {
        int length = (name != null ? name.getBytes().length : 0) + 1;
        length += (mac != null ? mac.getBytes().length : 0) + 1;
        length += (secret != null ? secret.getBytes().length : 0) + 1;
        length += (mId != null ? mId.getBytes().length : 0) + 1;
        return (short) (length + 24);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        try {
            yetUnknown1 = rawDataBuffer.getInt();
            name = getNextString(rawDataBuffer);
            mac = getNextString(rawDataBuffer);
            secret = getNextString(rawDataBuffer);
            yetUnknown2 = rawDataBuffer.getInt();
            mId = getNextString(rawDataBuffer);
            yetUnknown3 = rawDataBuffer.getInt();
            firmwareVersion = rawDataBuffer.getInt();
            yetUnknown4 = rawDataBuffer.getInt();
        } catch (Exception e) {
            System.out.println("Could not handle buffer wit data " + StringUtils.bytesToHex(rawDataBuffer.array()));
        }
    }

    @Override
    public short getType() {
        return WithingsStructureType.PROBE_REPLY;
    }
}
