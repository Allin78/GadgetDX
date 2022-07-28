package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

/**
 * This interface is the common denominator for all messages passed to and from the Steel HR.
 *
 */
public interface Message {
    List<WithingsStructure> getDataStructures();
    void addDataStructure(WithingsStructure data);
    short getType();
    byte[] getRawData();
    boolean needsResponse();
    boolean needsEOT();
    boolean isIncomingMessage();
    <T extends WithingsStructure> T getStructureByType(Class<T> type);
}
