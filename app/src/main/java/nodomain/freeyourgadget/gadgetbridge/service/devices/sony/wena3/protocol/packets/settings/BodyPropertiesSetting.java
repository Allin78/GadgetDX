package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.GenderSetting;

public class BodyPropertiesSetting implements Wena3Packetable {
    public GenderSetting gender;
    public short yearOfBirth;
    public short monthOfBirth;
    public short dayOfBirth;
    public short height;
    public short weight;

    public BodyPropertiesSetting(GenderSetting gender, short yearOfBirth, short monthOfBirth, short dayOfBirth, short height, short weight) {
        this.gender = gender;
        this.yearOfBirth = yearOfBirth;
        this.monthOfBirth = monthOfBirth;
        this.dayOfBirth = dayOfBirth;
        this.height = height;
        this.weight = weight;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(10)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x1D)
                .put((byte) gender.ordinal())
                .putShort(yearOfBirth)
                .put((byte) (monthOfBirth + 1)) // Java uses 0-indexed months
                .put((byte) dayOfBirth)
                .putShort((short) (height * 10))
                .putShort((short) (weight * 10))
                .array();
    }
}

