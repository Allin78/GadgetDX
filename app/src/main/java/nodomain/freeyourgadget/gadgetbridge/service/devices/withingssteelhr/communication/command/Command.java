package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

/**
 * This interface is the common denominator for all commands passed to the Steel HR.
 *
 */
public interface Command {
    void addDataStructure(WithingsStructure data);
    short getType();
    byte[] getRawData();
}
