package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol;

public enum Opcode {
    GET_DEVICE_INFO(0x02),
    ANC(0x08),
    SETTINGS(0xF2),
    AUTH_CHALLENGE(0x50),
    AUTH_CONFIRM(0x51),
    UNKNOWN(0xFF);

    private final byte opcode;

    Opcode(final int opcode) {
        this.opcode = (byte) opcode;
    }

    public byte getOpcode() {
        return opcode;
    }

    public static Opcode fromCode(final byte code) {
        for (final Opcode opcode : values()) {
            if (opcode.opcode == code) {
                return opcode;
            }
        }
        return Opcode.UNKNOWN;
    }
}
