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
    ArrayList<BluetoothGattCharacteristic> discoveredCharacteristics = new ArrayList<>();

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
    public void onServicesDiscovered(BluetoothGatt gatt) {
        for(BluetoothGattService service : gatt.getServices()) {
            discoveredCharacteristics.addAll(service.getCharacteristics());
        }
        super.onServicesDiscovered(gatt);
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
        BluetoothGattCharacteristic batteryCharacteristic = getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_BATTERY_LEVEL);

        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        if(batteryCharacteristic != null) {
            logger.debug("found battery characteristic!");
            builder.read(batteryCharacteristic);
        }

        int filters = BluetoothGattCharacteristic.PROPERTY_NOTIFY;
        for(BluetoothGattCharacteristic characteristic : discoveredCharacteristics) {
            if((characteristic.getProperties() | filters) != filters) {
                continue;
            }
            builder.notify(characteristic, true);
        }

        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));

        return builder;
    }
}
