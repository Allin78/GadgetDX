package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3Constants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.NotificationArrival;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.NotificationRemoval;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.LedColor;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationFlags;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.VibrationKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.VibrationOptions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.CameraAppTypeSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.TimeSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.TimeZoneSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.DeviceStateInfo;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.MusicInfo;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.NotificationServiceStatusRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.NotificationServiceStatusRequestType;

public class SonyWena3DeviceSupport extends AbstractBTLEDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(SonyWena3DeviceSupport.class);
    private String lastMusicInfo = null;
    public SonyWena3DeviceSupport() {
        super(LoggerFactory.getLogger(SonyWena3DeviceSupport.class));
        addSupportedService(SonyWena3Constants.COMMON_SERVICE_UUID);
        addSupportedService(SonyWena3Constants.NOTIFICATION_SERVICE_UUID);
    }
    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // Sync current time to device
        sendCurrentTime(builder);

        // Sync camera mode to device
        builder.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                CameraAppTypeSetting.findOut(getContext().getPackageManager()).toByteArray()
        );


        // Get battery state
        builder.read(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID));

        // Subscribe to updates
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_INFO_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_MODE_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID), true);


        // TODO: init and all

        getDevice().setFirmwareVersion("???");
        getDevice().setFirmwareVersion2("??2");


        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        return builder;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(characteristic.getUuid().equals(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID)) {
            DeviceStateInfo stateInfo = new DeviceStateInfo(characteristic.getValue());
            getDevice().setBatteryLevel(stateInfo.batteryPercentage);
            return true;
        }
        else if (characteristic.getUuid().equals(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID)) {
            NotificationServiceStatusRequest request = new NotificationServiceStatusRequest(characteristic.getValue());
            if(request.requestType == NotificationServiceStatusRequestType.MUSIC_INFO_FETCH.value) {
                LOG.debug("Request for music info received");
                sendMusicInfo(lastMusicInfo);
                return true;
            }
            else if(request.requestType == NotificationServiceStatusRequestType.LOCATE_PHONE.value) {
                LOG.debug("Request for find phone received");
                GBDeviceEventFindPhone findPhoneEvent = new GBDeviceEventFindPhone();
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.START;
                evaluateGBDeviceEvent(findPhoneEvent);
                return true;
            }
            else {
                LOG.warn("Unknown NotificationServiceStatusRequest %i", request.requestType);
            }
        }
        return false;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(characteristic.getUuid().equals(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID)) {
            DeviceStateInfo stateInfo = new DeviceStateInfo(characteristic.getValue());
            getDevice().setBatteryLevel(stateInfo.batteryPercentage);
            return true;
        }
        return false;
    }

    private void sendCurrentTime(@Nullable TransactionBuilder b) {
        try {
            TransactionBuilder builder = b == null ? performInitialized("updateDateTime") : b;

            TimeZone tz = TimeZone.getDefault();
            Date currentTime = new Date();

            builder.write(
                    getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                    new TimeSetting(currentTime).toByteArray()
            );

            builder.write(
                    getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                    new TimeZoneSetting(tz, currentTime).toByteArray()
            );

            if(b == null) performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send current time", e);
        }
    }

    private void sendMusicInfo(@Nullable String musicInfo) {
        try {
            TransactionBuilder builder = performInitialized("updateMusic");

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    new MusicInfo(musicInfo != null ? musicInfo: "").toByteArray()
            );

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send music info", e);
        }
    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
        StringBuilder sb = new StringBuilder();
        boolean hasTrackName = musicSpec.track != null && musicSpec.track.trim().length() > 0;
        boolean hasArtistName = musicSpec.artist != null && musicSpec.artist.trim().length() > 0;

        if(hasTrackName) {
            sb.append(musicSpec.track.trim());
        }
        if(hasArtistName && hasArtistName) {
            sb.append(" / ");
        }
        if(hasArtistName) {
            sb.append(musicSpec.artist.trim());
        }

        lastMusicInfo = sb.toString();
        sendMusicInfo(lastMusicInfo);
    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
        if(stateSpec.state == MusicStateSpec.STATE_PLAYING && lastMusicInfo != null) {
            sendMusicInfo(lastMusicInfo);
        } else if (stateSpec.state == MusicStateSpec.STATE_STOPPED) {
            sendMusicInfo("");
        }
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        try {
            TransactionBuilder builder = performInitialized("sendNotify");

            StringBuilder bodyBuilder = new StringBuilder();

            if(notificationSpec.sender != null && notificationSpec.sender.length() > 0) {
                bodyBuilder.append(notificationSpec.sender);
                bodyBuilder.append(":");
            }

            if(notificationSpec.title != null && notificationSpec.title.length() > 0) {
                if(bodyBuilder.length() > 0) {
                    bodyBuilder.append("\n");
                }
                bodyBuilder.append("[ ");
                bodyBuilder.append(notificationSpec.title);
                bodyBuilder.append(" ]");
            }

            if(notificationSpec.subject != null && notificationSpec.subject.length() > 0) {
                if(bodyBuilder.length() > 0) {
                    bodyBuilder.append("\n");
                }
                bodyBuilder.append("- ");
                bodyBuilder.append(notificationSpec.subject);
            }

            if(notificationSpec.body != null) {
                if(bodyBuilder.length() > 0) {
                    bodyBuilder.append("\n");
                }
                bodyBuilder.append(notificationSpec.body);
            }

            String actionLabel = notificationSpec.attachedActions.isEmpty() ? "" :
                    notificationSpec.attachedActions.get(0).title;

            boolean hasAction = !notificationSpec.attachedActions.isEmpty();

            NotificationFlags flags = NotificationFlags.NONE;
            // TODO: Figure out how actions work

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    new NotificationArrival(
                            NotificationKind.APP,
                            notificationSpec.getId(),
                            notificationSpec.sourceName,
                            bodyBuilder.toString(),
                            actionLabel,
                            new Date(notificationSpec.when),
                            new VibrationOptions(VibrationKind.STEP_UP, 1, false),
                            LedColor.GREEN,
                            flags
                    ).toByteArray()
            );

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send notification", e);
        }
    }

    @Override
    public void onDeleteNotification(int id) {
        try {
            TransactionBuilder builder = performInitialized("delNotify");

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    new NotificationRemoval(NotificationKind.APP, id).toByteArray()
            );

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send notification", e);
        }
    }


}
