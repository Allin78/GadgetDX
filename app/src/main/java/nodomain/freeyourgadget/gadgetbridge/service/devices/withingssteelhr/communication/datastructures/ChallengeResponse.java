package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ChallengeResponse extends WithingsStructure {

    private byte[] response = new byte[0];

    public byte[] getResponse() {
        return response;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    @Override
    public short getLength() {
        return (short) ((response != null ? response.length : 0) + 5);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        addByteArrayWithLengthByte(buffer, response);
    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        response = getNextByteArray(rawDataBuffer);
    }

    @Override
    public short getType() {
        return WithingsStructureType.CHALLENGE_RESPONSE;
    }

}
