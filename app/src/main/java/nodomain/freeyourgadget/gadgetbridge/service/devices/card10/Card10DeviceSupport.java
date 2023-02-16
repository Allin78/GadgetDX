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
package nodomain.freeyourgadget.gadgetbridge.service.devices.card10;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.card10.Card10Constants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.IntentListener;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfoProfile;

public class Card10DeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(Card10DeviceSupport.class);

    private final DeviceInfoProfile<Card10DeviceSupport> deviceInfoProfile;
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();

    private Timer vibraTimer;

    public Card10DeviceSupport() {
        super(LOG);

        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_HUMAN_INTERFACE_DEVICE);
        addSupportedService(Card10Constants.UUID_SERVICE_FILE_TRANSFER);
        addSupportedService(Card10Constants.UUID_SERVICE_CARD10);
        addSupportedService(Card10Constants.UUID_SERVICE_ECG);

        IntentListener mListener = intent -> {
            String action = intent.getAction();
            if (DeviceInfoProfile.ACTION_DEVICE_INFO.equals(action)) {
                handleDeviceInfo(intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
            }
        };

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        deviceInfoProfile.addListener(mListener);
        addSupportedProfile(deviceInfoProfile);
    }

    private void handleDeviceInfo(DeviceInfo info) {
        String fw = info.getFirmwareRevision();
        versionCmd.fwVersion = fw == null ? "N/A" : fw;

        handleGBDeviceEvent(versionCmd);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        // mark the device as initializing
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        deviceInfoProfile.requestDeviceInfo(builder);

        // ... custom initialization logic ...

        // set device firmware to prevent the following error when you (later) try to save data to database and
        // device firmware has not been set yet
        // Error executing 'the bind value at index 2 is null'java.lang.IllegalArgumentException: the bind value at index 2 is null
        getDevice().setFirmwareVersion("N/A");
        getDevice().setFirmwareVersion2("N/A");

        // mark the device as initialized
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));

        if (GBApplication.getPrefs().getBoolean("datetime_synconconnect", true)) {
            writeTime(builder);
        }

        return builder;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        if (characteristic.getUuid().equals(GattCharacteristic.UUID_CHARACTERISTIC_FIRMWARE_REVISION_STRING)) {
            getDevice().setFirmwareVersion(characteristic.getStringValue(0));
            return true;
        }

        return false;
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    public void onSetTime() {
        writeTime(null);
    }

    private void writeTime(@Nullable TransactionBuilder builder) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(System.currentTimeMillis());
        byte[] time = buffer.array();
        if (builder == null) {
            write("onSetTime", new CDPair(Card10Constants.UUID_CHARACTERISTIC_TIME_UPDATE, time));
        } else {
            write(builder, new CDPair(Card10Constants.UUID_CHARACTERISTIC_TIME_UPDATE, time));
        }
    }

    @Override
    public void onFindDevice(boolean start) {

        byte[] flashOff = {(byte) 0, (byte) 0, (byte) 0};
        byte[] rocketsOn = {(byte) (0x31 & 0xff), (byte) (0x31 & 0xff), (byte) (0x31 & 0xff)};

        if (start) {
            if (vibraTimer != null) {
                vibraTimer.cancel();
            }
            vibraTimer = new Timer(true);
            vibraTimer.schedule(new TimerTask() {

                private boolean flash = true;

                @Override
                public void run() {
                    // see https://firmware.card10.badge.events.ccc.de/bluetooth/card10.html#vibra-characteristic
                    // byte[] vibraData = {(byte) 250, (byte) 0}; // LSB first
                    byte[] vibraData = BLETypeConversions.fromUint16(250);

                    byte[] flashData = flash ? rocketsOn : flashOff;
                    flash = !flash;

                    write("onFindDevice",
                            new CDPair(Card10Constants.UUID_CHARACTERISTIC_VIBRA, vibraData),
                            new CDPair(Card10Constants.UUID_CHARACTERISTIC_ROCKETS, flashData)
                    );

                }
            }, 0, 1000);
        } else {
            vibraTimer.cancel();
            write("turnOffRockets",
                    new CDPair(Card10Constants.UUID_CHARACTERISTIC_ROCKETS, flashOff));
        }
    }

    private void setPersonalState(@NonNull PersonalState personalState) {
        LOG.debug("Setting personal state to " + personalState);
        write("setPersonalState",
                new CDPair(Card10Constants.UUID_CHARACTERISTIC_PERSONAL_STATE, personalState.getCommand()));
    }

    private void write(@NonNull String taskName, @NonNull CDPair... pairs) {
        try {
            TransactionBuilder builder = performInitialized(taskName);
            write(builder, pairs);
            builder.queue(getQueue());
        } catch (IOException e) {
            LOG.error("Unable to execute task " + taskName, e);
        }
    }

    private void write(@NonNull TransactionBuilder builder, @NonNull CDPair... pairs) {
        for (CDPair pair : pairs) {
            builder.write(getCharacteristic(pair.getCharacteristic()), pair.getData());
        }
    }
}
