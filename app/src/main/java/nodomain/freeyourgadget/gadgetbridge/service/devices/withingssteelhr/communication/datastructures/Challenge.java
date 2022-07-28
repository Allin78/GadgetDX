package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Challenge extends WithingsStructure {

    private String macAddress;

    private byte[] challenge;

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    @Override
    public short getLength() {
        int challengeLength = 0;
        int macAddressLength = (macAddress != null ? macAddress.getBytes().length : 0) + 1;
        if (challenge != null) {
            challengeLength = challenge.length;
        }

        return (short) (macAddressLength + challengeLength + 5);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        addStringAsBytesWithLengthByte(buffer, macAddress);

        if (challenge != null) {
            buffer.put((byte) challenge.length);
            buffer.put(challenge);
        } else {
            buffer.put((byte)0);
        }
    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        macAddress = getNextString(rawDataBuffer);
        challenge = getNextByteArray(rawDataBuffer);
    }

    @Override
    public short getType() {
        return WithingsStructureType.CHALLENGE;
    }


}
