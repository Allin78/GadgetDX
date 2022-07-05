package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

import static org.junit.Assert.*;

import org.junit.Test;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsTestStructure;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class AbstractCommandTest {

    @Test
    public void testGetRawDataNoData() {
        // arrange
        Command testCommand = createTestcommand();

        // act
        byte[] rawData = testCommand.getRawData();

        // assert
        assertEquals("0100630000", StringUtils.bytesToHex(rawData));
    }

    @Test
    public void testGetRawDataWithData() {
        // arrange
        Command testCommand = createTestcommand();
        testCommand.addDataStructure(new WithingsTestStructure());

        // act
        byte[] rawData = testCommand.getRawData();

        // assert
        assertEquals("0100630006006354657374", StringUtils.bytesToHex(rawData));
    }

    private Command createTestcommand() {
        return new AbstractCommand(){
            @Override
            public short getType() {
                    return 99;
            }
        };
    }
}