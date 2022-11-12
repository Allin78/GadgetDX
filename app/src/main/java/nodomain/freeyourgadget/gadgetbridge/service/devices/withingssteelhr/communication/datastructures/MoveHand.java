package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class MoveHand extends WithingsStructure {

    private short hand;
    private short movement;

    public void setHand(short hand) {
        this.hand = hand;
    }

    public void setMovement(short movement) {
        this.movement = movement;
    }

    @Override
    public short getLength() {
        return 7;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put((byte) (hand & 255));
        buffer.putShort(movement);
    }

    @Override
    public short getType() {
        return WithingsStructureType.MOVE_HAND;
    }
}
