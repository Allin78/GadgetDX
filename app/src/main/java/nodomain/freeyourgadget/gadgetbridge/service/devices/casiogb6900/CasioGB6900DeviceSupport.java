/*      Copyright (C) 2018 Andreas Böhler
        based on code from BlueWatcher, https://github.com/masterjc/bluewatcher

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.casiogb6900;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.devices.casiogb6900.CasioGB6900Constants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class CasioGB6900DeviceSupport extends AbstractBTLEDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(CasioGB6900DeviceSupport.class);

    public BluetoothGattCharacteristic mCasioCharact1 = null;
    public BluetoothGattCharacteristic mCasioCharact2 = null;
    public BluetoothGattCharacteristic mCasioCharact3 = null;
    public BluetoothGattCharacteristic mCasioCharact4 = null;
    public BluetoothGattCharacteristic mCasioCharact5 = null;
    private CasioGATTThread mThread = null;

    public CasioGB6900DeviceSupport() {
        super(LOG);
        addSupportedService(CasioGB6900Constants.CASIO_VIRTUAL_SERVER_SERVICE);
        addSupportedService(CasioGB6900Constants.ALERT_SERVICE_UUID);
        addSupportedService(CasioGB6900Constants.CASIO_IMMEDIATE_ALERT_SERVICE_UUID);
        addSupportedService(CasioGB6900Constants.CURRENT_TIME_SERVICE_UUID);
        addSupportedService(CasioGB6900Constants.WATCH_CTRL_SERVICE_UUID);
        addSupportedService(CasioGB6900Constants.WATCH_FEATURES_SERVICE_UUID);
        addSupportedService(CasioGB6900Constants.CASIO_PHONE_ALERT_STATUS_SERVICE);
        mThread = new CasioGATTThread(getContext(), this);
    }

    @Override
    public void setContext(GBDevice gbDevice, BluetoothAdapter btAdapter, Context context) {
        super.setContext(gbDevice, btAdapter, context);
        mThread.setContext(context);
        mThread.start();
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        LOG.info("Initializing");

        gbDevice.setState(GBDevice.State.INITIALIZING);
        gbDevice.sendDeviceUpdateIntent(getContext());

        mCasioCharact1 = getCharacteristic(CasioGB6900Constants.CASIO_A_NOT_COM_SET_NOT);
        mCasioCharact2 = getCharacteristic(CasioGB6900Constants.CASIO_A_NOT_W_REQ_NOT);
        mCasioCharact3 = getCharacteristic(CasioGB6900Constants.FUNCTION_SWITCH_CHARACTERISTIC);
        mCasioCharact4 = getCharacteristic(CasioGB6900Constants.ALERT_LEVEL_CHARACTERISTIC_UUID);
        mCasioCharact5 = getCharacteristic(CasioGB6900Constants.RINGER_CONTROL_POINT);

        builder.setGattCallback(this);
        builder.notify(mCasioCharact1, true);
        builder.notify(mCasioCharact2, true);
        builder.notify(mCasioCharact3, true);
        builder.notify(mCasioCharact4, true);
        builder.notify(mCasioCharact5, true);

        LOG.info("Initialization Done");

        return builder;
    }

    private void writeCasioCurrentTime(TransactionBuilder builder)
    {
        byte[] arr = new byte[10];
        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        arr[0] = (byte)((year >>> 0) & 0xff);
        arr[1] = (byte)((year >>> 8) & 0xff);
        arr[2] = (byte)(1 + cal.get(Calendar.MONTH));
        arr[3] = (byte)cal.get(Calendar.DAY_OF_MONTH);
        arr[4] = (byte)cal.get(Calendar.HOUR_OF_DAY);
        arr[5] = (byte)cal.get(Calendar.MINUTE);
        arr[6] = (byte)(1 + cal.get(Calendar.SECOND));
        byte dayOfWk = (byte)(cal.get(Calendar.DAY_OF_WEEK) - 1);
        if(dayOfWk == 0)
            dayOfWk = 7;
        arr[7] = dayOfWk;
        arr[8] = (byte)(int) TimeUnit.MILLISECONDS.toSeconds(256 * cal.get(Calendar.MILLISECOND));
        arr[9] = 1; // or 0?

        BluetoothGattCharacteristic charact = getCharacteristic(CasioGB6900Constants.CURRENT_TIME_CHARACTERISTIC_UUID);
        if(charact != null) {
            charact.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            builder.write(charact, arr);
        }
        else {
            LOG.warn("Characteristic not found: CURRENT_TIME_CHARACTERISTIC_UUID");
        }
    }

    private void writeCasioLocalTimeInformation(TransactionBuilder builder)
    {
        Calendar cal = Calendar.getInstance();
        int zoneOffset = (int)TimeUnit.MILLISECONDS.toMinutes(cal.get(Calendar.ZONE_OFFSET));
        int dstOffset = (int)TimeUnit.MILLISECONDS.toMinutes(cal.get(Calendar.DST_OFFSET));
        byte byte0 = (byte)(zoneOffset / 15);
        byte byte1 = (byte)(dstOffset / 15);
        BluetoothGattCharacteristic charact = getCharacteristic(CasioGB6900Constants.LOCAL_TIME_CHARACTERISTIC_UUID);
        if(charact != null) {
            builder.write(charact, new byte[]{byte0, byte1});
        }
        else {
            LOG.warn("Characteristic not found: LOCAL_TIME_CHARACTERISTIC_UUID");
        }

    }

    private void writeCasioVirtualServerFeature(TransactionBuilder builder)
    {
        byte byte0 = (byte)0;
        byte0 |= 1; // Casio Current Time Service
        byte0 |= 2; // Casio Alert Notification Service
        byte0 |= 4; // Casio Phone Alert Status Service
        byte0 |= 8; // Casio Immediate Alert Service

        BluetoothGattCharacteristic charact = getCharacteristic(CasioGB6900Constants.CASIO_VIRTUAL_SERVER_FEATURES);
        if(charact != null) {
            builder.write(charact, new byte[]{byte0, 0x00});
        }
        else {
            LOG.warn("Characteristic not found: CASIO_VIRTUAL_SERVER_FEATURES");
        }
    }

    private boolean handleCasioCom(byte[] data)
    {
        boolean handled = false;
        switch(data[0]) // ServiceID - actually an int
        {
            case 0:
                switch(data[2])
                {
                    case (byte) 1:
                        LOG.info("Initialization done, setting state to INITIALIZED");
                        gbDevice.setState(GBDevice.State.INITIALIZED);
                        gbDevice.sendDeviceUpdateIntent(getContext());
                        break;
                }
                break;
            case 2:
                switch(data[2]) // Request Type
                {
                    case (byte) 1:
                        try
                        {
                            TransactionBuilder builder = createTransactionBuilder("writeCasioCurrentTime");
                            writeCasioCurrentTime(builder);
                            performImmediately(builder);
                            handled = true;
                        } catch (IOException e) {
                            LOG.warn(e.getMessage());
                        }
                        break;
                    case (byte) 2:
                        try
                        {
                            TransactionBuilder builder = createTransactionBuilder("writeCasioLocalTimeInformation");
                            writeCasioLocalTimeInformation(builder);
                            performImmediately(builder);
                            handled = true;
                        } catch (IOException e) {
                            LOG.warn(e.getMessage());
                        }
                        break;
                }
                break;
            case 7:
                try
                {
                    TransactionBuilder builder = createTransactionBuilder("writeCasioVirtualServerFeature");
                    writeCasioVirtualServerFeature(builder);
                    performImmediately(builder);
                    handled = true;
                } catch (IOException e) {
                    LOG.warn(e.getMessage());
                }
                break;
        }
        return handled;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        boolean handled = false;

        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }

        UUID characteristicUUID = characteristic.getUuid();
        byte[] data = characteristic.getValue();
        if (data.length == 0)
            return true;

        if(characteristicUUID.equals(CasioGB6900Constants.CASIO_A_NOT_W_REQ_NOT))
        {
            handled = handleCasioCom(data);
        }

        if(characteristicUUID.equals(CasioGB6900Constants.CASIO_A_NOT_COM_SET_NOT))
        {
            handled = handleCasioCom(data);
        }

        if(characteristicUUID.equals(CasioGB6900Constants.ALERT_LEVEL_CHARACTERISTIC_UUID))
        {
            GBDeviceEventFindPhone findPhoneEvent = new GBDeviceEventFindPhone();
            if(data[0] == 0x02) {
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.START;
            }
            else
            {
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.STOP;
            }
                evaluateGBDeviceEvent(findPhoneEvent);
            handled = true;
        }

        if(characteristicUUID.equals(CasioGB6900Constants.RINGER_CONTROL_POINT))
        {
            if(data[0] == 0x02)
            {
                LOG.info("Mute/ignore call event not yet supported by GB");
            }
            handled = true;
        }

        if(!handled)
        {
            LOG.info("Unhandled characteristic change: " + characteristicUUID + " code: " + String.format("0x%1x ...", data[0]));
        }
        return true;
    }

    private void showNotification(byte icon, String title, String message) {
        try {
            TransactionBuilder builder = performInitialized("showNotification");
            int len;

            byte[] titleBytes = title.getBytes(StandardCharsets.US_ASCII);
            len = titleBytes.length > 18 ? 18 : titleBytes.length;
            byte[] msg = new byte[2 + len];
            msg[0] = icon;
            msg[1] = 1;
            for(int i=0; i<len; i++)
            {
                msg[i + 2] = titleBytes[i];
            }

            builder.write(getCharacteristic(CasioGB6900Constants.ALERT_CHARACTERISTIC_UUID), msg);
            LOG.info("Showing notification, title: " + title + " message (not sent): " + message);
            performConnected(builder.getTransaction());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
        }
    }

    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        String notificationTitle = StringUtils.getFirstOf(notificationSpec.sender, notificationSpec.title);
        byte icon;
        switch (notificationSpec.type) {
            case GENERIC_SMS:
                icon = CasioGB6900Constants.SMS_NOTIFICATION_ID;
                break;
            case GENERIC_CALENDAR:
                icon = CasioGB6900Constants.CALENDAR_NOTIFICATION_ID;
                break;
            case GENERIC_EMAIL:
                icon = CasioGB6900Constants.MAIL_NOTIFICATION_ID;
                break;
            default:
                icon = CasioGB6900Constants.SNS_NOTIFICATION_ID;
                break;
        }
        showNotification(icon, notificationTitle, notificationSpec.body);
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {

    }

    @Override
    public void onSetTime() {
        try {
            TransactionBuilder builder = performInitialized("SetTime");
            writeCasioLocalTimeInformation(builder);
            writeCasioCurrentTime(builder);
            performConnected(builder.getTransaction());
        } catch(IOException e) {
            LOG.warn(e.getMessage());
        }
    }

    @Override
    public void onSetCallState(CallSpec callSpec) {
        switch (callSpec.command) {
            case CallSpec.CALL_INCOMING:
                showNotification(CasioGB6900Constants.CALL_NOTIFICATION_ID, callSpec.name, callSpec.number);
                break;
        }
    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {

    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {

    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {

    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppInfoReq() {

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {

    }

    @Override
    public void onAppDelete(UUID uuid) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onAppReorder(UUID[] uuids) {

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {

    }

    @Override
    public void onReset(int flags) {
        try {

        } catch(Exception e) {
            LOG.warn(e.getMessage());
        }
    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {

    }

    @Override
    public void onFindDevice(boolean start) {
        if(start) {
            showNotification(CasioGB6900Constants.SNS_NOTIFICATION_ID, "You found it!", "");
        }
    }

    @Override
    public void onSetConstantVibration(int integer) {

    }

    @Override
    public void onScreenshotReq() {

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {

    }

    @Override
    public void onSendConfiguration(String config) {

    }

    @Override
    public void onTestNewFunction() {

    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {

    }
}
