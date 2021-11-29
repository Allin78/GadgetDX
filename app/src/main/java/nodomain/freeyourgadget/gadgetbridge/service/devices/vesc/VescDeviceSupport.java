package nodomain.freeyourgadget.gadgetbridge.service.devices.vesc;

import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.logging.Logger;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;

public class VescDeviceSupport extends VescBaseDeviceSupport{
    BluetoothGattCharacteristic serialWriteCharacteristic;
    public VescDeviceSupport(Logger logger) {
        super(logger);
    }

    public VescDeviceSupport(Logger logger, BluetoothGattCharacteristic serialWriteCharacteristic){
        super(logger);
        this.serialWriteCharacteristic = serialWriteCharacteristic;
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        return builder
                .add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }

    public void setCurrent(int currentMillisAmperes){
        buildAndQueryPacket(CommandType.SET_CURRENT, currentMillisAmperes);
    }

    public void setBreakCurrent(int breakCurrentMillisAmperes){
        buildAndQueryPacket(CommandType.SET_CURRENT_BRAKE, breakCurrentMillisAmperes);
    }

    public void setRPM(int rpm){
        buildAndQueryPacket(CommandType.SET_RPM, rpm);
    }

    public void buildAndQueryPacket(CommandType commandType, Object ... args){
        byte[] data = buildPacket(commandType, args);
        queryPacket(data);
    }

    public void queryPacket(byte[] data){
        new TransactionBuilder("write serial packet")
                .write(this.serialWriteCharacteristic, data)
                .queue(getQueue());
    }

    public byte[] buildPacket(CommandType commandType, Object ... args){
        int dataLength = 0;
        for(Object arg : args){
            if(arg instanceof Integer) dataLength += 4;
            else if(arg instanceof Short) dataLength += 2;
        }
        ByteBuffer buffer = ByteBuffer.allocate(dataLength);

        for(Object arg : args){
            if(arg instanceof Integer) buffer.putInt((Integer) arg);
            if(arg instanceof Short) buffer.putShort((Short) arg);
        }

        return buildPacket(commandType, buffer.array());
    }

    public byte[] buildPacket(CommandType commandType, byte[] data){
        return buildPacket(commandType.getCommandByte(), data);
    }

    private byte[] buildPacket(byte commandByte, byte[] data){
        byte[] contents = new byte[data.length + 1];
        contents[0] = commandByte;
        System.arraycopy(data, 0, contents, 1, data.length);
        return buildPacket(contents);
    }

    private byte[] buildPacket(byte[] contents){
        int dataLength = contents.length;
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + dataLength < 256 ? 3: 4);
        if(dataLength < 256){
            buffer.put((byte)0x02);
            buffer.put((byte)dataLength);
        }else{
            buffer.put((byte) 0x03);
            buffer.putShort((short) dataLength);
        }
        buffer.put(contents);
        buffer.putShort((short) Utils.calculate_crc(contents));
        buffer.put((byte) 0x03);

        return buffer.array();
    }
}
