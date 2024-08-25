package nodomain.freeyourgadget.gadgetbridge.service.devices.gatt_client;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;

public class BleGattClientSupport extends AbstractBTLEDeviceSupport {
    public static final Logger logger = LoggerFactory.getLogger(BleGattClientSupport.class);

    public BleGattClientSupport() {
        super(logger);

        addSupportedService(GattService.UUID_SERVICE_BATTERY_SERVICE);
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(characteristic.getUuid().equals(GattCharacteristic.UUID_CHARACTERISTIC_BATTERY_LEVEL)) {
            int level = characteristic.getValue()[0];
            getDevice().setBatteryLevel(level);
            getDevice().sendDeviceUpdateIntent(getContext());
        }
        return super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        BluetoothGattCharacteristic batteryCharacteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_BATTERY_LEVEL);

        if(batteryCharacteristic != null) {
            logger.debug("found battery characteristic!");
            builder.read(batteryCharacteristic);
        }

        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));

        return builder;
    }
}
