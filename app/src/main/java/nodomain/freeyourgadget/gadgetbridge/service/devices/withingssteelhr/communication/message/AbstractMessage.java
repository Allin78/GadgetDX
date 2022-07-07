package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

public abstract class AbstractMessage implements Message {

    /**
     * The header consist of the first byte 0x01 (probably the message format identifier),
     * two bytes for the message type and 2 bytes for the actual datalength.
     */
    private static final int HEADER_SIZE = 5;
    private List<WithingsStructure> dataStructures = new ArrayList<WithingsStructure>();

    public List<WithingsStructure> getDataStructures() {
        return Collections.unmodifiableList(dataStructures);
    }

    @Override
    public void addDataStructure(WithingsStructure data) {
        dataStructures.add(data);
    }

    @Override
    public byte[] getRawData() {
        short structureLength = 0;
        for (WithingsStructure structure : dataStructures) {
            structureLength = (short)(structure.getLength());
        }
        ByteBuffer rawDataBuffer = ByteBuffer.allocate(HEADER_SIZE + structureLength);
        rawDataBuffer.put((byte)0x01); // <= This seems to be always 0x01 for all commands
        rawDataBuffer.putShort(getType());
        rawDataBuffer.putShort(structureLength);

        for (WithingsStructure structure : dataStructures) {
            rawDataBuffer.put(structure.getRawData());
        }
        return rawDataBuffer.array();
    }
}
