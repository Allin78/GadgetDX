package nodomain.freeyourgadget.gadgetbridge.service.devices.vesc;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.ByteBuffer;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.devices.vesc.VescCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;

public class VescDeviceSupport extends VescBaseDeviceSupport{
    BluetoothGattCharacteristic serialWriteCharacteristic;

    public static final String COMMAND_SET_RPM = "nodomain.freeyourgadget.gadgetbridge.vesc.command.SET_RPM";
    public static final String COMMAND_SET_CURRENT = "nodomain.freeyourgadget.gadgetbridge.vesc.command.SET_CURRENT";
    public static final String COMMAND_SET_BREAK_CURRENT = "nodomain.freeyourgadget.gadgetbridge.vesc.command.SET_BREAK_CURRENT";
    public static final String EXTRA_RPM = "EXTRA_RPM";
    public static final String EXTRA_CURRENT = "EXTRA_CURRENT";

    public VescDeviceSupport(){
        super();
        this.serialWriteCharacteristic = null;
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        initBroadcast();

        DeviceType type = getDevice().getType();

        if(type == DeviceType.VESC_NRF){
            this.serialWriteCharacteristic = getCharacteristic(UUID.fromString(VescCoordinator.UUID_SERIAL_NRF));
        }else if(type == DeviceType.VESC_HM10){
            this.serialWriteCharacteristic = getCharacteristic(UUID.fromString(VescCoordinator.UUID_SERIAL_HM10));
        }

        return builder
                .add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }

    private void initBroadcast() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(COMMAND_SET_RPM);
        filter.addAction(COMMAND_SET_CURRENT);
        filter.addAction(COMMAND_SET_BREAK_CURRENT);

        broadcastManager.registerReceiver(commandReceiver, filter);
    }

    BroadcastReceiver commandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(COMMAND_SET_RPM)){
                VescDeviceSupport.this.setRPM(
                        intent.getIntExtra(EXTRA_RPM, 0)
                );
            }else if(intent.getAction().equals(COMMAND_SET_BREAK_CURRENT)){
                VescDeviceSupport.this.setBreakCurrent(
                        intent.getIntExtra(EXTRA_CURRENT, 0)
                );
            }else if(intent.getAction().equals(COMMAND_SET_CURRENT)){
                VescDeviceSupport.this.setCurrent(
                        intent.getIntExtra(EXTRA_CURRENT, 0)
                );
            }
        }
    };

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
