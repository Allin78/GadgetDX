package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Message {

    public static final byte[] MESSAGE_HEADER = {(byte) 0xfe, (byte) 0xdc, (byte) 0xba};
    public static final byte MESSAGE_TRAILER = (byte) 0xef;

    private static final int MESSAGE_OFFSET = MESSAGE_HEADER.length;

    private final MessageType type;
    private final Opcode opcode;
    private final byte sequenceNumber;
    private final byte[] payload;

    public Message(final MessageType type, final Opcode opcode, final byte sequenceNumber, final byte[] payload) {
        this.type = type;
        this.opcode = opcode;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
    }

    public byte[] encode() {

        int size = (!type.isRequest()) ? 2 : 1;
        final ByteBuffer buf = ByteBuffer.allocate(payload.length + 8 + size);

        int payloadLength = payload.length + size;

        buf.order(ByteOrder.BIG_ENDIAN);

        buf.put(MESSAGE_HEADER);
        buf.put(type.getCode());
        buf.put(opcode.getOpcode());
        buf.put((byte) (payloadLength >> 8 & 0xff));
        buf.put((byte) (payloadLength & 0xff));
        if (!type.isRequest()) {
            buf.put((byte) 0x00);
        }
        buf.put(sequenceNumber);
        buf.put(payload);
        buf.put(MESSAGE_TRAILER);

        return buf.array();
    }

    public static Message fromBytes(byte[] message) {
        MessageType type = MessageType.fromCode(message[MESSAGE_OFFSET]);
        Opcode opcode = Opcode.fromCode(message[MESSAGE_OFFSET + 1]);
        short payloadLength = (short) (message[MESSAGE_OFFSET + 2] << 8 | message[MESSAGE_OFFSET + 3]);

        int payloadOffset = MESSAGE_OFFSET + ((!type.isRequest()) ? 6 : 5);
        byte sequenceNumber = message[payloadOffset - 1];

        int actualPayloadLength = message.length - payloadOffset - 1;
        byte[] payload = new byte[actualPayloadLength];
        System.arraycopy(message, payloadOffset, payload, 0, actualPayloadLength);
        return new Message(type, opcode, sequenceNumber, payload);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", opcode=" + opcode +
                ", sequenceNumber=" + sequenceNumber +
                ", payload=" + hexdump(payload) +
                '}';
    }

    public MessageType getType() {
        return type;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getPayload() {
        return payload;
    }
}
