package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

/**
 * This abstract class is the common denominator for all data structures used inside commands and the corresponding responses.
 * @see nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command.Command
 */
public abstract class WithingsStructure {
    final static short HEADER_SIZE = 4;
    public abstract short getLength();
    public abstract byte[] getRawData();
    abstract short getType();
    abstract void addSubStructure(WithingsStructure subStructure);
}
