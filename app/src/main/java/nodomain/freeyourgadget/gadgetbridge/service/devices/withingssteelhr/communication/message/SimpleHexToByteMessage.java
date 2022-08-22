package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import java.nio.ByteBuffer;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class SimpleHexToByteMessage implements Message {
    private String hexString;

    public SimpleHexToByteMessage(String hexString) {
        this.hexString = hexString;
    }

    @Override
    public List<WithingsStructure> getDataStructures() {
        return null;
    }

    @Override
    public void addDataStructure(WithingsStructure data) {

    }

    @Override
    public short getType() {
        return 0;
    }

    @Override
    public byte[] getRawData() {
        return GB.hexStringToByteArray(hexString);
    }

    @Override
    public boolean needsResponse() {
        return false;
    }

    @Override
    public boolean needsEOT() {
        return false;
    }

    @Override
    public boolean isIncomingMessage() {
        return false;
    }

    @Override
    public <T extends WithingsStructure> T getStructureByType(Class<T> type) {
        return null;
    }
}
