package nodomain.freeyourgadget.gadgetbridge.service.devices.vesc;

public enum CommandType {
    SET_CURRENT((byte) 0x06),
    SET_CURRENT_BRAKE((byte) 0x07),
    SET_RPM((byte) 0x08),
    ;
    byte commandByte;

    CommandType(byte commandByte){
        this.commandByte = commandByte;
    }

    public byte getCommandByte(){
        return this.commandByte;
    }
}
