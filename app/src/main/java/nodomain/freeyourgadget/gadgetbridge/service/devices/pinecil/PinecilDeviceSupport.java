/*  Copyright (C) 2023 Marc Nause

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.pinecil;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.devices.pinecil.PinecilConstants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;

public class PinecilDeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PinecilDeviceSupport.class);

    public PinecilDeviceSupport() {
        super(LOG);

        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);

        addSupportedService(PinecilConstants.UUID_SERVICE_BULK_DATA);
        addSupportedService(PinecilConstants.UUID_SERVICE_LIVE_DATA);
        addSupportedService(PinecilConstants.UUID_SERVICE_SETTINGS_DATA);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        // mark the device as initializing
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // ... custom initialization logic ...

        // set device firmware to prevent the following error when you (later) try to save data to database and
        // device firmware has not been set yet
        // Error executing 'the bind value at index 2 is null'java.lang.IllegalArgumentException: the bind value at index 2 is null
        getDevice().setFirmwareVersion("N/A");
        getDevice().setFirmwareVersion2("N/A");

        // mark the device as initialized
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));

        // request firmware version
        builder.read(getCharacteristic(PinecilConstants.UUID_CHARACTERISTIC_BULK_BUILD));

        // // lines below are just for trying things, will be removed later
        builder.read(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_DEVICE_NAME));
        builder.read(getCharacteristic(PinecilConstants.UUID_CHARACTERISTIC_LIVE_HANDLE_TEMP));
        builder.read(getCharacteristic(PinecilConstants.UUID_CHARACTERISTIC_LIVE_LIVE_TEMP));
        builder.read(getCharacteristic(PinecilConstants.UUID_CHARACTERISTIC_BULK_LIVE_DATA));
        builder.read(getCharacteristic(PinecilConstants.UUID_CHARACTERISTIC_SETTINGS_VALUE_1));

        return builder;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        LOG.info("SERVICE: " + characteristic.getService().getUuid());
        LOG.info("STATUS: " + status);

        UUID uuid = characteristic.getUuid();
        if (uuid.equals(PinecilConstants.UUID_CHARACTERISTIC_BULK_BUILD)) {
            getDevice().setFirmwareVersion(characteristic.getStringValue(0));
            getDevice().sendDeviceUpdateIntent(this.getContext());
            return true;
        }

        // lines below are just for trying things, will be removed later
        if (uuid.equals(PinecilConstants.UUID_CHARACTERISTIC_LIVE_HANDLE_TEMP)) {
            LOG.info("HANDLE TEMP: " + characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0) / 10f + "°C");
        } else if (uuid.equals(GattCharacteristic.UUID_CHARACTERISTIC_DEVICE_NAME)) {
            LOG.info("NAME: " + characteristic.getStringValue(0));
        } else {
            LOG.info("DATA: " + Arrays.toString(characteristic.getValue()));
            LOG.info("STRING: " + characteristic.getStringValue(0));
        }

        return false;
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

}
