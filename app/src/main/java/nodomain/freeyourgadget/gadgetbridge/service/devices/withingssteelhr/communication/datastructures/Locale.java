package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Locale extends WithingsStructure {

    private String locale = "en";

    public Locale(String locale) {
        this.locale = locale;
    }

    @Override
    public short getLength() {
        return (short) ((locale != null ? locale.getBytes().length : 0) + 1 + HEADER_SIZE);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer rawDataBuffer) {
        addStringAsBytesWithLengthByte(rawDataBuffer, locale);
    }

    @Override
    public short getType() {
        return WithingsStructureType.LOCALE;
    }
}
