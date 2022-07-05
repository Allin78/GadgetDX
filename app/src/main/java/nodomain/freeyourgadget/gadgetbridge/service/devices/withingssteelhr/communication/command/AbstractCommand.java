package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

public abstract class AbstractCommand implements Command {
    private static final int HEADER_SIZE = 3;
    private static final int STRUCTURE_LENGTH_SIZE = 2;
    private List<WithingsStructure> dataStructures = new ArrayList<WithingsStructure>();

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
        ByteBuffer rawDataBuffer = ByteBuffer.allocate(HEADER_SIZE + STRUCTURE_LENGTH_SIZE + structureLength);
        rawDataBuffer.put((byte)0x01); // <= This seems to be always 0x01 for all commands
        rawDataBuffer.putShort(getType());
        rawDataBuffer.putShort(structureLength);

        for (WithingsStructure structure : dataStructures) {
            rawDataBuffer.put(structure.getRawData());
        }
        return rawDataBuffer.array();
    }
}
