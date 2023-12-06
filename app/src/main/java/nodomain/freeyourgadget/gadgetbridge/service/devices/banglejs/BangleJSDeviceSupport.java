/*  Copyright (C) 2019-2024 Albert, Andreas Shimokawa, Arjan Schrijver, Damien
    Gaignon, Gabriele Monaco, Ganblejs, gfwilliams, glemco, Gordon Williams,
    halemmerich, illis, José Rebelo, Lukas, LukasEdl, Marc Nause, Martin Boonk,
    rarder44, Richard de Boer, Simon Sievert

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.banglejs;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_ALLOW_HIGH_MTU;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BANGLEJS_TEXT_BITMAP;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BANGLEJS_TEXT_BITMAP_SIZE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_UPDATE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_UPDATE_INTERVAL;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DEVICE_GPS_USE_NETWORK_ONLY;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DEVICE_INTENTS;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DEVICE_INTERNET_ACCESS;
import static nodomain.freeyourgadget.gadgetbridge.database.DBHelper.getUser;
import static nodomain.freeyourgadget.gadgetbridge.devices.banglejs.BangleJSConstants.PREF_BANGLEJS_ACTIVITY_FULL_SYNC_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.banglejs.BangleJSConstants.PREF_BANGLEJS_ACTIVITY_FULL_SYNC_STATUS;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SimpleTimeZone;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import de.greenrobot.dao.query.QueryBuilder;
import io.wax911.emojify.Emoji;
import io.wax911.emojify.EmojiManager;
import io.wax911.emojify.EmojiUtils;
import nodomain.freeyourgadget.gadgetbridge.BuildConfig;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.capabilities.loyaltycards.BarcodeFormat;
import nodomain.freeyourgadget.gadgetbridge.capabilities.loyaltycards.LoyaltyCard;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventCallControl;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventMusicControl;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventNotificationControl;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdatePreferences;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.banglejs.BangleJSConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.banglejs.BangleJSSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummary;
import nodomain.freeyourgadget.gadgetbridge.entities.CalendarSyncState;
import nodomain.freeyourgadget.gadgetbridge.entities.CalendarSyncStateDao;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.externalevents.CalendarReceiver;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.User;
import nodomain.freeyourgadget.gadgetbridge.export.ActivityTrackExporter;
import nodomain.freeyourgadget.gadgetbridge.export.GPXExporter;
import nodomain.freeyourgadget.gadgetbridge.externalevents.gps.GBLocationManager;
import nodomain.freeyourgadget.gadgetbridge.externalevents.gps.LocationProviderType;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityPoint;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityTrack;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.model.GPSCoordinate;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NavigationInfoSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationType;
import nodomain.freeyourgadget.gadgetbridge.model.RecordedDataTypes;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEQueue;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.util.DateTimeUtils;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;
import nodomain.freeyourgadget.gadgetbridge.util.EmojiConverter;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.LimitedQueue;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class BangleJSDeviceSupport extends AbstractBTLEDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(BangleJSDeviceSupport.class);

    private BluetoothGattCharacteristic rxCharacteristic = null;
    private BluetoothGattCharacteristic txCharacteristic = null;
    private boolean allowHighMTU = false;
    private int mtuSize = 20;
    int bangleCommandSeq = 0; // to attempt to stop duplicate packets when sending Local Intents

    /// Current line of data received from Bangle.js
    private String receivedLine = "";
    /// All characters received from Bangle.js for debug purposes (limited to MAX_RECEIVE_HISTORY_CHARS). Can be dumped with 'Fetch Device Debug Logs' from Debug menu
    private String receiveHistory = "";
    private boolean realtimeHRM = false;
    private boolean realtimeStep = false;
    /// How often should activity data be sent - in seconds
    private int realtimeHRMInterval = 10;
    /// Last battery percentage reported (or -1) to help with smoothing reported battery levels
    private int lastBatteryPercent = -1;

    private final LimitedQueue<Integer, Long> mNotificationReplyAction = new LimitedQueue<>(16);

    private boolean gpsUpdateSetup = false;

    // this stores the globalUartReceiver (for uart.tx intents)
    private BroadcastReceiver globalUartReceiver = null;

    // used to make HTTP requests and handle responses
    private RequestQueue requestQueue = null;

    /// Maximum amount of characters to store in receiveHistory
    public static final int MAX_RECEIVE_HISTORY_CHARS = 100000;
    /// Used to avoid spamming logs with ACTION_DEVICE_CHANGED messages
    static String lastStateString;

    // Local Intents - for app manager communication
    public static final String BANGLEJS_COMMAND_TX = "banglejs_command_tx";
    public static final String BANGLEJS_COMMAND_RX = "banglejs_command_rx";
    // Global Intents
    private static final String BANGLE_ACTION_UART_TX = "com.banglejs.uart.tx";

    public BangleJSDeviceSupport() {
        super(LOG);
        addSupportedService(BangleJSConstants.UUID_SERVICE_NORDIC_UART);

        registerLocalIntents();
        registerGlobalIntents();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopGlobalUartReceiver();
        stopLocationUpdate();
        stopRequestQueue();
    }

    private void stopGlobalUartReceiver(){
        if(globalUartReceiver != null){
            GBApplication.getContext().unregisterReceiver(globalUartReceiver); // remove uart.tx intent listener
        }
    }


    private void stopLocationUpdate() {
        if (!gpsUpdateSetup)
            return;
        LOG.info("Stop location updates");
        GBLocationManager.stop(getContext(), this);
        gpsUpdateSetup = false;
    }

    private void stopRequestQueue() {
        if (requestQueue != null) {
            requestQueue.stop();
        }
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getContext());
        }
        return requestQueue;
    }

    private void addReceiveHistory(String s) {
        receiveHistory += s;
        if (receiveHistory.length() > MAX_RECEIVE_HISTORY_CHARS)
            receiveHistory = receiveHistory.substring(receiveHistory.length() - MAX_RECEIVE_HISTORY_CHARS);
    }

    private void registerLocalIntents() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(GBDevice.ACTION_DEVICE_CHANGED);
        commandFilter.addAction(BANGLEJS_COMMAND_TX);
        BroadcastReceiver commandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BANGLEJS_COMMAND_TX: {
                        String data = String.valueOf(intent.getExtras().get("DATA"));
                        BtLEQueue queue = getQueue();
                        if (queue==null) {
                            LOG.warn("BANGLEJS_COMMAND_TX received, but getQueue()==null (state=" + gbDevice.getStateString() + ")");
                        } else {
                            try {
                                TransactionBuilder builder = performInitialized("TX");
                                uartTx(builder, data);
                                builder.queue(queue);
                            } catch (IOException e) {
                                GB.toast(getContext(), "Error in TX: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
                            }
                        }
                        break;
                    }
                    case GBDevice.ACTION_DEVICE_CHANGED: {
                        String stateString = (gbDevice!=null ? gbDevice.getStateString():"");
                        if (!stateString.equals(lastStateString)) {
                          lastStateString = stateString;
                          LOG.info("ACTION_DEVICE_CHANGED " + stateString);
                          addReceiveHistory("\n================================================\nACTION_DEVICE_CHANGED "+stateString+" "+(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)).format(Calendar.getInstance().getTime())+"\n================================================\n");
                        }
                        if (gbDevice!=null && (gbDevice.getState() == GBDevice.State.NOT_CONNECTED || gbDevice.getState() == GBDevice.State.WAITING_FOR_RECONNECT)) {
                            stopLocationUpdate();
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(GBApplication.getContext()).registerReceiver(commandReceiver, commandFilter);
    }

    private void registerGlobalIntents() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(BANGLE_ACTION_UART_TX);
        globalUartReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BANGLE_ACTION_UART_TX: {
                        /* In Tasker:
                          Action: com.banglejs.uart.tx
                          Cat: None
                          Extra: line:Terminal.println(%avariable)
                          Target: Broadcast Receiver

                          Variable: Number, Configure on Import, NOT structured, Value set, Nothing Exported, NOT Same as value
                         */
                        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
                        if (!devicePrefs.getBoolean(PREF_DEVICE_INTENTS, false)) return;
                        String data = intent.getStringExtra("line");
                        if (data==null) {
                            GB.toast(getContext(), "UART TX Intent, but no 'line' supplied", Toast.LENGTH_LONG, GB.ERROR);
                            return;
                        }
                        if (!data.endsWith("\n")) data += "\n";
                        try {
                            TransactionBuilder builder = performInitialized("TX");
                            uartTx(builder, data);
                            builder.queue(getQueue());
                        } catch (IOException e) {
                            GB.toast(getContext(), "Error in TX: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
                        }
                        break;
                    }
                }
            }
        };
        GBApplication.getContext().registerReceiver(globalUartReceiver, commandFilter); // should be RECEIVER_EXPORTED
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        LOG.info("Initializing");

        gbDevice.setState(GBDevice.State.INITIALIZING);
        gbDevice.sendDeviceUpdateIntent(getContext());
        gbDevice.setBatteryThresholdPercent((short) 15);

        rxCharacteristic = getCharacteristic(BangleJSConstants.UUID_CHARACTERISTIC_NORDIC_UART_RX);
        txCharacteristic = getCharacteristic(BangleJSConstants.UUID_CHARACTERISTIC_NORDIC_UART_TX);
        if (rxCharacteristic==null || txCharacteristic==null) {
            // https://codeberg.org/Freeyourgadget/Gadgetbridge/issues/2996 - sometimes we get
            // initializeDevice called but no characteristics have been fetched - try and reconnect in that case
            LOG.warn("RX/TX characteristics are null, will attempt to reconnect");
            builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.WAITING_FOR_RECONNECT, getContext()));
        }
        builder.setCallback(this);
        builder.notify(rxCharacteristic, true);

        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
        allowHighMTU = devicePrefs.getBoolean(PREF_ALLOW_HIGH_MTU, true);

        if (allowHighMTU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.requestMtu(131);
        }
        // No need to clear active line with Ctrl-C now - firmwares in 2023 auto-clear on connect

        Prefs prefs = GBApplication.getPrefs();
        if (prefs.getBoolean("datetime_synconconnect", true))
          transmitTime(builder);
        //sendSettings(builder);

        // get version
        gbDevice.setState(GBDevice.State.INITIALIZED);
        gbDevice.sendDeviceUpdateIntent(getContext());
        if (getDevice().getFirmwareVersion() == null) {
            getDevice().setFirmwareVersion("N/A");
            getDevice().setFirmwareVersion2("N/A");
        }
        lastBatteryPercent = -1;

        LOG.info("Initialization Done");

        requestBangleGPSPowerStatus();

        return builder;
    }

    /// Write a string of data, and chunk it up
    private void uartTx(TransactionBuilder builder, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
        LOG.info("UART TX: " + str);
        addReceiveHistory("\n================================================\nSENDING "+str+"\n================================================\n");
        // FIXME: somehow this is still giving us UTF8 data when we put images in strings. Maybe JSON.stringify is converting to UTF-8?
        for (int i=0;i<bytes.length;i+=mtuSize) {
            int l = bytes.length-i;
            if (l>mtuSize) l=mtuSize;
            byte[] packet = new byte[l];
            System.arraycopy(bytes, i, packet, 0, l);
            builder.write(txCharacteristic, packet);
        }
    }

    /// Converts an object to a JSON string. see jsonToString
    private String jsonToStringInternal(Object v) {
        if (v instanceof String) {
            /* Convert a string, escaping chars we can't send over out UART connection */
            String s = (String)v;
            StringBuilder json = new StringBuilder("\"");
            boolean hasUnicode = false;
            //String rawString = "";
            for (int i=0;i<s.length();i++) {
                int ch = (int)s.charAt(i); // unicode, so 0..65535 (usually)
                int nextCh = (int)(i+1<s.length() ? s.charAt(i+1) : 0); // 0..65535
                //rawString = rawString+ch+",";
                if (ch>255) hasUnicode = true;
                if (ch<8) {
                    // if the next character is a digit, it'd be interpreted
                    // as a 2 digit octal character, so we can't use `\0` to escape it
                    if (nextCh>='0' && nextCh<='7') json.append("\\x0").append(ch);
                    else json.append("\\").append(ch);
                } else if (ch==8) json.append("\\b");
                else if (ch==9) json.append("\\t");
                else if (ch==10) json.append("\\n");
                else if (ch==11) json.append("\\v");
                else if (ch==12) json.append("\\f");
                else if (ch==34) json.append("\\\""); // quote
                else if (ch==92) json.append("\\\\"); // slash
                else if (ch<32 || ch==127 || ch==173 ||
                         ((ch>=0xC2) && (ch<=0xF4))) // unicode start char range
                    json.append("\\x").append(Integer.toHexString((ch & 255) | 256).substring(1));
                else if (ch>255)
                    json.append("\\u").append(Integer.toHexString((ch & 65535) | 65536).substring(1));
                else json.append(s.charAt(i));
            }
            // if it was less characters to send base64, do that!
            if (!hasUnicode && (json.length() > 5+(s.length()*4/3))) {
                byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
                return "atob(\""+Base64.encodeToString(bytes, Base64.DEFAULT).replaceAll("\n","")+"\")";
            }
            // for debugging...
            //addReceiveHistory("\n---------------------\n"+rawString+"\n---------------------\n");
            return json.append("\"").toString();
        } else if (v instanceof JSONArray) {
            JSONArray a = (JSONArray)v;
            StringBuilder json = new StringBuilder("[");
            for (int i=0;i<a.length();i++) {
                if (i>0) json.append(",");
                Object o = null;
                try {
                    o = a.get(i);
                } catch (JSONException e) {
                    LOG.warn("jsonToString array error: " + e.getLocalizedMessage());
                }
                json.append(jsonToStringInternal(o));
            }
            return json.append("]").toString();
        } else if (v instanceof JSONObject) {
            JSONObject obj = (JSONObject)v;
            StringBuilder json = new StringBuilder("{");
            Iterator<String> iter = obj.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                Object o = null;
                try {
                    o = obj.get(key);
                } catch (JSONException e) {
                    LOG.warn("jsonToString object error: " + e.getLocalizedMessage());
                }
                json.append("\"").append(key).append("\":").append(jsonToStringInternal(o));
                if (iter.hasNext()) json.append(",");
            }
            return json.append("}").toString();
        } else if (v==null) {
            // else int/double/null
            return "null";
        }
        return v.toString();
    }

    /// Convert a JSON object to a JSON String (NOT 100% JSON compliant)
    public String jsonToString(JSONObject jsonObj) {
        /* jsonObj.toString() works but breaks char codes>128 (encodes as UTF8?) and also uses
        \u0000 when just \0 would do (and so on).

        So we do it manually, which can be more compact anyway.
        This is JSON-ish, so not exactly as per JSON1 spec but good enough for Espruino.
        */
        return jsonToStringInternal(jsonObj);
    }

    /// Write a JSON object of data
    private void uartTxJSON(String taskName, JSONObject json) {
        try {
            TransactionBuilder builder = performInitialized(taskName);
            uartTx(builder, "\u0010GB("+jsonToString(json)+")\n");
            builder.queue(getQueue());
        } catch (IOException e) {
            GB.toast(getContext(), "Error in "+taskName+": " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }

    private void uartTxJSONError(String taskName, String message, String id) {
        JSONObject o = new JSONObject();
        try {
            o.put("t", taskName);
            if( id!=null)
                o.put("id", id);
            o.put("err", message);
        } catch (JSONException e) {
            GB.toast(getContext(), "uartTxJSONError: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
        uartTxJSON(taskName, o);
    }



    private void handleUartRxLine(String line) {
        LOG.info("UART RX LINE: " + line);
        if (line.length()==0) return;
        if (">Uncaught ReferenceError: \"GB\" is not defined".equals(line))
          GB.toast(getContext(), "'Android Integration' plugin not installed on Bangle.js", Toast.LENGTH_LONG, GB.ERROR);
        else if (line.charAt(0)=='{') {
            // JSON - we hope!
            try {
                JSONObject json = new JSONObject(line);
                if (json.has("t")) {
                    handleUartRxJSON(json);
                    LOG.info("UART RX JSON parsed successfully");
                } else
                    LOG.warn("UART RX JSON parsed but doesn't contain 't' - ignoring");
            } catch (JSONException e) {
                LOG.error("UART RX JSON parse failure: "+ e.getLocalizedMessage());
                GB.toast(getContext(), "Malformed JSON from Bangle.js: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
            }

        } else {
            LOG.info("UART RX line started with "+(int)line.charAt(0)+" - ignoring");
        }
    }

    private void handleUartRxJSON(JSONObject json) throws JSONException {
        String packetType = json.getString("t");
        switch (packetType) {
            case "info":
                GB.toast(getContext(), "Bangle.js: " + json.getString("msg"), Toast.LENGTH_LONG, GB.INFO);
                break;
            case "warn":
                GB.toast(getContext(), "Bangle.js: " + json.getString("msg"), Toast.LENGTH_LONG, GB.WARN);
                break;
            case "error":
                GB.toast(getContext(), "Bangle.js: " + json.getString("msg"), Toast.LENGTH_LONG, GB.ERROR);
                break;
            case "ver": {
                final GBDeviceEventVersionInfo gbDeviceEventVersionInfo = new GBDeviceEventVersionInfo();
                if (json.has("fw"))
                    gbDeviceEventVersionInfo.fwVersion = json.getString("fw");
                if (json.has("hw"))
                    gbDeviceEventVersionInfo.hwVersion = json.getString("hw");
                evaluateGBDeviceEvent(gbDeviceEventVersionInfo);
            } break;
            case "findPhone": {
                boolean start = json.has("n") && json.getBoolean("n");
                GBDeviceEventFindPhone deviceEventFindPhone = new GBDeviceEventFindPhone();
                deviceEventFindPhone.event = start ? GBDeviceEventFindPhone.Event.START : GBDeviceEventFindPhone.Event.STOP;
                evaluateGBDeviceEvent(deviceEventFindPhone);
            } break;
            case "music": {
                GBDeviceEventMusicControl deviceEventMusicControl = new GBDeviceEventMusicControl();
                deviceEventMusicControl.event = GBDeviceEventMusicControl.Event.valueOf(json.getString("n").toUpperCase());
                evaluateGBDeviceEvent(deviceEventMusicControl);
            } break;
            case "call": {
                GBDeviceEventCallControl deviceEventCallControl = new GBDeviceEventCallControl();
                deviceEventCallControl.event = GBDeviceEventCallControl.Event.valueOf(json.getString("n").toUpperCase());
                evaluateGBDeviceEvent(deviceEventCallControl);
            } break;
            case "status":
                handleBatteryStatus(json);
                break;
            case "notify" :
                handleNotificationControl(json);
                break;
            case "actfetch":
                handleActivityFetch(json);
                break;
            case "act":
                handleActivity(json);
                break;
            case "trksList":
                handleTrksList(json);
                break;
            case "actTrk":
                handleActTrk(json);
                break;
            case "http":
                handleHttp(json);
                break;
            case "force_calendar_sync":
                handleCalendarSync(json);
                break;
            case "intent":
                handleIntent(json);
                break;
            case "gps_power": {
                boolean status = json.getBoolean("status");
                LOG.info("Got gps power status: " + status);
                if (status) {
                    setupGPSUpdateTimer();
                } else {
                    stopLocationUpdate();
                }
            } break;
            default : {
                LOG.info("UART RX JSON packet type '"+packetType+"' not understood.");
            }
        }
    }

    /**
     * Handle "status" packets: battery info updates
     */
    private void handleBatteryStatus(JSONObject json) throws JSONException {
        GBDeviceEventBatteryInfo batteryInfo = new GBDeviceEventBatteryInfo();
        batteryInfo.state = BatteryState.UNKNOWN;
        if (json.has("chg")) {
            batteryInfo.state = (json.getInt("chg") == 1) ? BatteryState.BATTERY_CHARGING : BatteryState.BATTERY_NORMAL;
        }
        if (json.has("bat")) {
            int b = json.getInt("bat");
            if (b < 0) b = 0;
            if (b > 100) b = 100;
            // smooth out battery level reporting (it can only go up if charging, or down if discharging)
            // http://forum.espruino.com/conversations/379294
            if (lastBatteryPercent<0) lastBatteryPercent = b;
            if (batteryInfo.state == BatteryState.BATTERY_NORMAL && b > lastBatteryPercent)
                b = lastBatteryPercent;
            if (batteryInfo.state == BatteryState.BATTERY_CHARGING && b < lastBatteryPercent)
                b = lastBatteryPercent;
            lastBatteryPercent = b;
            batteryInfo.level = b;
        }

        if (json.has("volt"))
            batteryInfo.voltage = (float) json.getDouble("volt");
        handleGBDeviceEvent(batteryInfo);
    }

    /**
     * Handle "notify" packet, used to send notification control from device to GB
     */
    private void handleNotificationControl(JSONObject json) throws JSONException {
        GBDeviceEventNotificationControl deviceEvtNotificationControl = new GBDeviceEventNotificationControl();
        // .title appears unused
        deviceEvtNotificationControl.event = GBDeviceEventNotificationControl.Event.valueOf(json.getString("n").toUpperCase());
        if (json.has("id"))
            deviceEvtNotificationControl.handle = json.getInt("id");
        if (json.has("tel"))
            deviceEvtNotificationControl.phoneNumber = json.getString("tel");
        if (json.has("msg"))
            deviceEvtNotificationControl.reply = json.getString("msg");
        /* REPLY responses don't use the ID from the event (MUTE/etc seem to), but instead
         * they use a handle that was provided in an action list on the onNotification.. event  */
        if (deviceEvtNotificationControl.event == GBDeviceEventNotificationControl.Event.REPLY) {
            Long foundHandle = mNotificationReplyAction.lookup((int)deviceEvtNotificationControl.handle);
            if (foundHandle!=null)
                deviceEvtNotificationControl.handle = foundHandle;
        }
        evaluateGBDeviceEvent(deviceEvtNotificationControl);
    }

    private void handleActivityFetch(final JSONObject json) throws JSONException {
        final String state = json.getString("state");
        if ("start".equals(state)) {
            GB.updateTransferNotification(getContext().getString(R.string.busy_task_fetch_activity_data),"", true, 0, getContext());
            getDevice().setBusyTask(getContext().getString(R.string.busy_task_fetch_activity_data));
        } else if ("end".equals(state)) {
            saveLastSyncTimestamp(System.currentTimeMillis() - 1000L * 60);
            getDevice().unsetBusyTask();
            GB.updateTransferNotification(null, "", false, 100, getContext());
        } else {
            LOG.warn("Unknown actfetch state {}", state);
        }

        final GBDeviceEventUpdatePreferences event = new GBDeviceEventUpdatePreferences()
                .withPreference(PREF_BANGLEJS_ACTIVITY_FULL_SYNC_STATUS, state);
        evaluateGBDeviceEvent(event);

        getDevice().sendDeviceUpdateIntent(getContext());
    }

    /**
     * Handle "act" packet, used to send activity reports
     */
    private void handleActivity(JSONObject json) {
        BangleJSActivitySample sample = new BangleJSActivitySample();
        int timestamp = (int) (json.optLong("ts", System.currentTimeMillis()) / 1000);
        int hrm = json.optInt("hrm", 0);
        int steps = json.optInt("stp", 0);
        int intensity = json.optInt("mov", ActivitySample.NOT_MEASURED);
        boolean realtime = json.optInt("rt", 0) == 1;
        int activity = BangleJSSampleProvider.TYPE_ACTIVITY;
        /*if (json.has("act")) {
            String actName = "TYPE_" + json.getString("act").toUpperCase();
            try {
                Field f = ActivityKind.class.getField(actName);
                try {
                    activity = f.getInt(null);
                } catch (IllegalAccessException e) {
                    LOG.info("JSON activity '"+actName+"' not readable");
                }
            } catch (NoSuchFieldException e) {
                LOG.info("JSON activity '"+actName+"' not found");
            }
        }*/
        sample.setTimestamp(timestamp);
        sample.setRawKind(activity);
        sample.setHeartRate(hrm);
        sample.setSteps(steps);
        sample.setRawIntensity(intensity);
        if (!realtime) {
            try (DBHandler dbHandler = GBApplication.acquireDB()) {
                final Long userId = getUser(dbHandler.getDaoSession()).getId();
                final Long deviceId = DBHelper.getDevice(getDevice(), dbHandler.getDaoSession()).getId();
                BangleJSSampleProvider provider = new BangleJSSampleProvider(getDevice(), dbHandler.getDaoSession());
                sample.setDeviceId(deviceId);
                sample.setUserId(userId);
                provider.upsertSample(sample);
            } catch (final Exception ex) {
                LOG.warn("Error saving activity: " + ex.getLocalizedMessage());
            }
        }

        // push realtime data
        if (realtime && (realtimeHRM || realtimeStep)) {
            Intent intent = new Intent(DeviceService.ACTION_REALTIME_SAMPLES)
                    .putExtra(DeviceService.EXTRA_REALTIME_SAMPLE, sample);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
    }

    private void handleTrksList(JSONObject json) throws JSONException {
        LOG.info("trksList says hi!");
        //GB.toast(getContext(), "trksList says hi!", Toast.LENGTH_LONG, GB.INFO);
        JSONArray tracksList = json.getJSONArray("list");
        LOG.info("New recorder logs since last fetch: " + String.valueOf(tracksList));
        for (int i = 0; i < tracksList.length(); i ++) {
            requestActivityTrackLog(tracksList.getString(i), i==tracksList.length()-1);
        }
    }

    private void handleActTrk(JSONObject json) throws JSONException {
        LOG.info("actTrk says hi!");
        //GB.toast(getContext(), "actTrk says hi!", Toast.LENGTH_LONG, GB.INFO);
        String log = json.getString("log");
        String line = json.getString("line");
        LOG.info(log);
        LOG.info(line);
        File dir;
        try {
            dir = FileUtils.getExternalFilesDir();
        } catch (IOException e) {
            return;
        }
        String filename = "recorder.log" + log + ".csv";

        if (line.equals("end of recorder log")) { // TODO: Persist log to database here by reading the now completely transferred csv file from GB storage directory

            File inputFile = new File(dir, filename);
            try { // FIXME: There is maybe code inside this try-statement that should be outside of it.

                // Read from the previously stored log (see the else-statement below) into a string.
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                StringBuilder storedLogBuilder = new StringBuilder(reader.readLine() + "\n");
                while ((line = reader.readLine()) != null) {
                    storedLogBuilder.append(line).append("\n");
                }
                reader.close();
                String storedLog = String.valueOf(storedLogBuilder);
                storedLog = storedLog.replace(",",", "); // So all rows (internal arrays) in storedLogArray2 get the same number of entries.
                LOG.info("Contents of log read from GB storage:\n" + storedLog);

                // Turn the string log into a 2d array in two steps.
                String[] storedLogArray = storedLog.split("\n") ;
                String[][] storedLogArray2 = new String[storedLogArray.length][1];

                for (int i = 0; i < storedLogArray.length; i++) {
                    storedLogArray2[i] = storedLogArray[i].split(",");
                    for (int j = 0; j < storedLogArray2[i].length;j++) {
                        storedLogArray2[i][j] = storedLogArray2[i][j].trim(); // Remove the extra spaces we introduced above for getting the same number of entries on all rows.
                    }
                }

                LOG.info("Contents of storedLogArray2:\n" + Arrays.deepToString(storedLogArray2));

                // Turn the 2d array into an object for easier access later on.
                JSONObject storedLogObject = new JSONObject();
                JSONArray valueArray = new JSONArray();
                for (int i = 0; i < storedLogArray2[0].length; i++){
                    for (int j = 1; j < storedLogArray2.length; j++) {
                        valueArray.put(storedLogArray2[j][i]);
                    }
                    storedLogObject.put(storedLogArray2[0][i], valueArray);
                    valueArray = new JSONArray();
                }

                LOG.info("storedLogObject:\n" + storedLogObject);

                // Calculate and store analytical data (distance, speed, cadence, etc.).
                JSONObject analyticsObject = new JSONObject();
                JSONArray calculationsArray = new JSONArray();
                int logLength = storedLogObject.getJSONArray("Time").length();

                // Add elapsed time since first reading (seconds).
                valueArray = storedLogObject.getJSONArray("Time");
                for (int i = 0; i < logLength; i++) {
                    calculationsArray.put(valueArray.getDouble(i)-valueArray.getDouble(0));
                }
                analyticsObject.put("Elapsed Time", calculationsArray);

                valueArray = new JSONArray();
                calculationsArray = new JSONArray();

                JSONArray valueArray2 = new JSONArray();

                // Add analytics based on GPS coordinates.
                if (storedLogObject.has("Latitude")) {
                    // Add distance between last and current reading.
                    valueArray = storedLogObject.getJSONArray("Latitude");
                    valueArray2 = storedLogObject.getJSONArray("Longitude");
                    for (int i = 0; i < logLength; i++) {
                        if (i == 0) {
                            calculationsArray.put("0");
                        } else {
                            String distance;
                            if (Objects.equals(valueArray.getString(i), "") || Objects.equals(valueArray.getString(i - 1), "")) {
                                // FIXME: GPS data can be missing for some entries which is handled here.
                                // Should use more complex logic to be more accurate. Use interpolation.
                                // Should distances be done via the GPX file we generate instead?
                                distance = "0.001";
                            } else {
                                distance = distanceFromCoordinatePairs(
                                        (String) valueArray.get(i - 1),
                                        (String) valueArray2.get(i - 1),
                                        (String) valueArray.get(i),
                                        (String) valueArray2.get(i)
                                );
                            }
                            calculationsArray.put(distance);
                        }
                    }
                    analyticsObject.put("Intermediate Distance", calculationsArray);

                    valueArray = new JSONArray();
                    valueArray2 = new JSONArray();
                    calculationsArray = new JSONArray();

                    // Add stride lengths between consecutive readings.
                    if (storedLogObject.has("Steps")) {
                        for (int i = 0; i < logLength; i++) {
                            if (i == 0) {
                                calculationsArray.put("0");
                            } else {
                                double steps = storedLogObject.getJSONArray("Steps").getDouble(i);
                                if (steps==0) steps=0.001;
                                double calculation =
                                        2 * analyticsObject.getJSONArray("Intermediate Distance").getDouble(i) / steps;
                                calculationsArray.put(calculation);
                            }
                        }
                        analyticsObject.put("Stride", calculationsArray);

                        calculationsArray = new JSONArray();
                    }

                } else if (storedLogObject.has("Steps")) {
                    for (int i = 0; i < logLength; i++) {
                        if (i==0) {
                            calculationsArray.put(0);
                        } else {
                            double stride = 0.85; // TODO: Depend on user defined stride length?
                            double calculation = stride * (storedLogObject.getJSONArray("Steps").getDouble(i));
                            //if (calculation == 0) calculation = 0.001; // To avoid potential division by zero later on.
                            calculationsArray.put(calculation);
                        }
                    }
                    analyticsObject.put("Intermediate Distance", calculationsArray);

                    calculationsArray = new JSONArray();

                }

                if (analyticsObject.has("Intermediate Distance")) {
                    // Add total distance from start of activity up to each reading.
                    for (int i = 0; i < logLength; i++) {
                       if (i==0) {
                           calculationsArray.put(0);
                       } else {
                           double calculation = calculationsArray.getDouble(i-1) + analyticsObject.getJSONArray("Intermediate Distance").getDouble(i);
                           calculationsArray.put(calculation);
                       }
                    }
                    analyticsObject.put("Total Distance", calculationsArray);

                    calculationsArray = new JSONArray();

                    // Add average speed between last and current reading (m/s).
                    for (int i = 0; i < logLength; i++) {
                        if (i==0) {
                            calculationsArray.put(0);
                        } else {
                            double timeDiff =
                                    (analyticsObject.getJSONArray("Elapsed Time").getDouble(i) -
                                    analyticsObject.getJSONArray("Elapsed Time").getDouble(i-1));
                            if (timeDiff==0) timeDiff = 1;
                            double calculation =
                                    analyticsObject.getJSONArray("Intermediate Distance").getDouble(i) / timeDiff;
                            calculationsArray.put(calculation);
                        }
                    }
                    analyticsObject.put("Speed", calculationsArray);

                    calculationsArray = new JSONArray();

                    // Add average pace between last and current reading (s/km). (Was gonna do this as min/km but summary seems to expect s/km).
                    for (int i = 0; i < logLength; i++) {
                        if (i==0) {
                            calculationsArray.put(0);
                        } else {
                            double speed = analyticsObject.getJSONArray("Speed").getDouble(i);
                            if (speed==0) speed = 0.001;
                            double calculation = (1000.0) * 1/speed;
                            calculationsArray.put(calculation);
                        }
                    }
                    analyticsObject.put("Pace", calculationsArray);

                    calculationsArray = new JSONArray();
                }

                if (storedLogObject.has("Steps")) {
                    for (int i = 0; i < logLength; i++) {
                        if (i==0) {
                            calculationsArray.put(0);
                        } else {
                            // FIXME: Should cadence be steps/min or half that? https://www.polar.com/blog/what-is-running-cadence/
                            // The Bangle.js App Loader has Cadence = (steps/min)/2,  https://github.com/espruino/BangleApps/blob/master/apps/recorder/interface.html#L103,
                            // as discussed here: https://github.com/espruino/BangleApps/pull/3068#issuecomment-1790293879 .
                            double timeDiff =
                                    (storedLogObject.getJSONArray("Time").getDouble(i) -
                                            storedLogObject.getJSONArray("Time").getDouble(i-1));
                            if (timeDiff==0) timeDiff = 1;
                            double calculation = 0.5 * 60 *
                                    (storedLogObject.getJSONArray("Steps").getDouble(i) / timeDiff);
                            calculationsArray.put(calculation);
                        }
                    }
                    analyticsObject.put("Cadence", calculationsArray);

                    calculationsArray = new JSONArray();
                }
                LOG.info("AnalyticsObject:\n" + analyticsObject);

                BaseActivitySummary summary = null;

                Date startTime = new Date(Long.parseLong(storedLogArray2[1][0].split("\\.\\d")[0])*1000L);
                Date endTime = new Date(Long.parseLong(storedLogArray2[storedLogArray2.length-1][0].split("\\.\\d")[0])*1000L);
                summary = new BaseActivitySummary();
                summary.setName(log);
                summary.setStartTime(startTime);
                summary.setEndTime(endTime);
                summary.setActivityKind(ActivityKind.TYPE_RUNNING); // TODO: Make this depend on info from watch (currently this info isn't supplied in Bangle.js recorder logs).
                summary.setRawDetailsPath(String.valueOf(inputFile));

                JSONObject summaryData = new JSONObject();
                summaryData = addSummaryData(summaryData,"test",3,"mm");
       //            private JSONObject createActivitySummaryGroups(){
       // final Map<String, List<String>> groupDefinitions = new HashMap<String, List<String>>() {{
       //     put("Strokes", Arrays.asList(
       //             "averageStrokeDistance", "averageStrokesPerSecond", "strokes"
       //     ));

       //     put("Swimming", Arrays.asList(
       //             "swolfIndex", "swimStyle"
       //     ));

       //     put("Elevation", Arrays.asList(
       //             "ascentMeters", "descentMeters", "maxAltitude", "minAltitude", "averageAltitude",
       //             "baseAltitude", "ascentSeconds", "descentSeconds", "flatSeconds", "ascentDistance",
       //             "descentDistance", "flatDistance", "elevationGain", "elevationLoss"
       //     ));
                if (storedLogObject.has("Altitude") || storedLogObject.has("Barometer Altitude")) {
                    summaryData = addSummaryData(summaryData, "ascentMeters", 3, "mm");
                    summaryData = addSummaryData(summaryData, "descentMeters", 3, "mm");
                    summaryData = addSummaryData(summaryData, "maxAltitude", 3, "mm");
                    summaryData = addSummaryData(summaryData, "minAltitude", 3, "mm");
                    summaryData = addSummaryData(summaryData, "averageAltitude", 3, "mm");
                    summaryData = addSummaryData(summaryData, "baseAltitude", 3, "mm");
                    summaryData = addSummaryData(summaryData, "ascentSeconds", 3, "mm");
                    summaryData = addSummaryData(summaryData, "descentSeconds", 3, "mm");
                    summaryData = addSummaryData(summaryData, "flatSeconds", 3, "mm");
                    if (analyticsObject.has("Intermittent Distance")) {
                        summaryData = addSummaryData(summaryData, "ascentDistance", 3, "mm");
                        summaryData = addSummaryData(summaryData, "descentDistance", 3, "mm");
                        summaryData = addSummaryData(summaryData, "flatDistance", 3, "mm");
                    }
                    summaryData = addSummaryData(summaryData, "elevationGain", 3, "mm");
                    summaryData = addSummaryData(summaryData, "elevationLoss", 3, "mm");
                }
       //     put("Speed", Arrays.asList(
       //             "averageSpeed", "maxSpeed", "minSpeed", "averageKMPaceSeconds", "minPace",
       //             "maxPace", "averageSpeed2", "averageCadence", "maxCadence", "minCadence"
       //     ));
                try {
                    if (analyticsObject.has("Speed")) {
                        //summaryData = addSummaryData(summaryData,"averageSpeed",averageOfJSONArray(analyticsObject.getJSONArray("Speed")),"mm"); // This seems to be calculated somewhere else automatically.
                        summaryData = addSummaryData(summaryData, "maxSpeed", maxOfJSONArray(analyticsObject.getJSONArray("Speed")), "m/s");
                        summaryData = addSummaryData(summaryData, "minSpeed", maxOfJSONArray(analyticsObject.getJSONArray("Speed")), "m/s");
                        //summaryData = addSummaryData(summaryData, "averageKMPaceSeconds", averageOfJSONArray(analyticsObject.getJSONArray("Pace")), "s/km"); // Is this also calculated automatically then?
                        //summaryData = addSummaryData(summaryData, "averageKMPaceSeconds",
                        //        (float) (1000.0 * analyticsObject.getJSONArray("Elapsed Time").getDouble(logLength-1) /
                        //                analyticsObject.getJSONArray("Total Distance").getDouble(logLength-1)),
                        //        "s/km"
                        //);
                        summaryData = addSummaryData(summaryData, "minPace", minOfJSONArray(analyticsObject.getJSONArray("Pace")), "s/km");
                        summaryData = addSummaryData(summaryData, "maxPace", maxOfJSONArray(analyticsObject.getJSONArray("Pace")), "s/km");
                        //summaryData = addSummaryData(summaryData,"averageSpeed2",3,"mm");
                    }
                    if (analyticsObject.has("Cadence")) {
                        summaryData = addSummaryData(summaryData, "averageCadence",
                                60 * sumOfJSONArray(storedLogObject.getJSONArray("Steps")) /
                                        (float) analyticsObject.getJSONArray("Elapsed Time").getDouble(logLength - 1),
                                "steps/min"
                        );
                        summaryData = addSummaryData(summaryData, "maxCadence", maxOfJSONArray(analyticsObject.getJSONArray("Cadence")), "steps/min");
                        summaryData = addSummaryData(summaryData, "minCadence", minOfJSONArray(analyticsObject.getJSONArray("Cadence")), "steps/min");
                    }
                    //     put("Activity", Arrays.asList(
                    //             "distanceMeters", "steps", "activeSeconds", "caloriesBurnt", "totalStride",
                    //             "averageHR", "maxHR", "minHR", "averageStride", "maxStride", "minStride"
                    //     ));
                    if (analyticsObject.has("Intermediate Distance")) summaryData =
                            addSummaryData(summaryData, "distanceMeters",
                                    (float) analyticsObject.getJSONArray("Total Distance").getDouble(logLength - 1),
                                    "m");
                    if (storedLogObject.has("Steps"))
                        summaryData = addSummaryData(summaryData, "steps", sumOfJSONArray(storedLogObject.getJSONArray("Steps")), "steps");
                    //summaryData = addSummaryData(summaryData,"activeSeconds",3,"mm"); // FIXME: Is this suppose to exclude the time of inactivity in a workout?
                    //summaryData = addSummaryData(summaryData,"caloriesBurnt",3,"mm"); // TODO: Should this be calculated on Gadgetbridge side or be reported by Bangle.js?
                    //summaryData = addSummaryData(summaryData,"totalStride",3,"mm"); // FIXME: What is this?
                    if (storedLogObject.has("Heartrate")) {
                        summaryData = addSummaryData(summaryData, "averageHR", averageOfJSONArray(storedLogObject.getJSONArray("Heartrate")), "bpm");
                        summaryData = addSummaryData(summaryData, "maxHR", maxOfJSONArray(storedLogObject.getJSONArray("Heartrate")), "bpm");
                        summaryData = addSummaryData(summaryData, "minHR", minOfJSONArray(storedLogObject.getJSONArray("Heartrate")), "bpm");
                    }
                    if (analyticsObject.has("Stride")) {
                        summaryData = addSummaryData(summaryData, "averageStride",
                                (float) (analyticsObject.getJSONArray("Total Distance").getDouble(logLength - 1) /
                                        (0.5 * sumOfJSONArray(storedLogObject.getJSONArray("Steps")))),
                                "m/stride"); // FIXME: Is this meant to be stride length as I've assumed?
                        summaryData = addSummaryData(summaryData, "maxStride", maxOfJSONArray(analyticsObject.getJSONArray("Stride")), "m/stride");
                        summaryData = addSummaryData(summaryData, "minStride", minOfJSONArray(analyticsObject.getJSONArray("Stride")), "m/stride");
                    }
                } catch (Exception e) {
                    LOG.error(String.valueOf(e) + ". (thrown when trying to add summary data");
                }
       //     put("HeartRateZones", Arrays.asList(
       //             "hrZoneNa", "hrZoneWarmUp", "hrZoneFatBurn", "hrZoneAerobic", "hrZoneAnaerobic",
       //             "hrZoneExtreme"
       //     ));
                // TODO: Implement hrZones by doing calculations on Gadgetbridge side or make Bangle.js report this (Karvonen method implemented to a degree in watch app "Run+")?
                //summaryData = addSummaryData(summaryData,"hrZoneNa",3,"mm");
                //summaryData = addSummaryData(summaryData,"hrZoneWarmUp",3,"mm");
                //summaryData = addSummaryData(summaryData,"hrZoneFatBurn",3,"mm");
                //summaryData = addSummaryData(summaryData,"hrZoneAerobic",3,"mm");
                //summaryData = addSummaryData(summaryData,"hrZoneAnaerobic",3,"mm");
                //summaryData = addSummaryData(summaryData,"hrZoneExtreme",3,"mm");
       //     put("TrainingEffect", Arrays.asList(
       //             "aerobicTrainingEffect", "anaerobicTrainingEffect", "currentWorkoutLoad",
       //             "maximumOxygenUptake"
       //     ));

       //     put("Laps", Arrays.asList(
       //             "averageLapPace", "laps"
       //     ));
                // TODO: Does Bangle.js report laps in recorder logs?
                //summaryData = addSummaryData(summaryData,"averageLapPace",3,"mm");
                //summaryData = addSummaryData(summaryData,"laps",3,"mm");
       // }};
                summary.setSummaryData(summaryData.toString());

                ActivityTrack track = new ActivityTrack(); // detailsParser.parse(buffer.toByteArray());
                track.startNewSegment();
                track.setBaseTime(startTime);
                track.setName(log);
                try (DBHandler dbHandler = GBApplication.acquireDB()) {
                    DaoSession session = dbHandler.getDaoSession();
                    Device device = DBHelper.getDevice(getDevice(), session);
                    User user = DBHelper.getUser(session);
                    track.setDevice(device);
                    track.setUser(user);
                } catch (Exception ex) {
                    GB.toast(getContext(), "Error setting user for activity track.", Toast.LENGTH_LONG, GB.ERROR, ex);
                }
                ActivityPoint point = new ActivityPoint();
                Date timeOfPoint = new Date();
                for (int i = 0; i < storedLogObject.getJSONArray("Time").length(); i++) {
                    timeOfPoint.setTime(storedLogObject.getJSONArray("Time").getLong(i)*1000L);
                    point.setTime(timeOfPoint);
                    if (storedLogObject.has("Longitude")) {
                        if (!Objects.equals(storedLogObject.getJSONArray("Longitude").getString(i), "")
                                && !Objects.equals(storedLogObject.getJSONArray("Latitude").getString(i), "")
                                && !Objects.equals(storedLogObject.getJSONArray("Altitude").getString(i), "")) {

                            point.setLocation(new GPSCoordinate(
                                            storedLogObject.getJSONArray("Longitude").getDouble(i),
                                            storedLogObject.getJSONArray("Latitude").getDouble(i),
                                            storedLogObject.getJSONArray("Altitude").getDouble(i)
                                    )
                            );
                        }
                    }
                    if (storedLogObject.has("Heartrate") && !Objects.equals(storedLogObject.getJSONArray("Heartrate").getString(i), "")) {
                        point.setHeartRate(storedLogObject.getJSONArray("Heartrate").getInt(i));
                    }
                    track.addTrackPoint(point);
                    LOG.info("Activity Point:\n" + point.getHeartRate());
                    point = new ActivityPoint();
                }

                ActivityTrackExporter exporter = createExporter();
                String trackType = "track";
                switch (summary.getActivityKind()) {
                    case ActivityKind.TYPE_CYCLING:
                        trackType = getContext().getString(R.string.activity_type_biking);
                        break;
                    case ActivityKind.TYPE_RUNNING:
                        trackType = getContext().getString(R.string.activity_type_running);
                        break;
                    case ActivityKind.TYPE_WALKING:
                        trackType = getContext().getString(R.string.activity_type_walking);
                        break;
                    case ActivityKind.TYPE_HIKING:
                        trackType = getContext().getString(R.string.activity_type_hiking);
                        break;
                    case ActivityKind.TYPE_CLIMBING:
                        trackType = getContext().getString(R.string.activity_type_climbing);
                        break;
                    case ActivityKind.TYPE_SWIMMING:
                        trackType = getContext().getString(R.string.activity_type_swimming);
                        break;
                }

                String fileName = FileUtils.makeValidFileName("gadgetbridge-" + trackType.toLowerCase() + "-" + summary.getName() + ".gpx");
                File targetFile = new File(FileUtils.getExternalFilesDir(), fileName);

                try {
                    exporter.performExport(track, targetFile);

                    try (DBHandler dbHandler = GBApplication.acquireDB()) {
                        summary.setGpxTrack(targetFile.getAbsolutePath());
                        //dbHandler.getDaoSession().getBaseActivitySummaryDao().update(summary);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (ActivityTrackExporter.GPXTrackEmptyException ex) {
                    GB.toast(getContext(), "This activity does not contain GPX tracks.", Toast.LENGTH_LONG, GB.ERROR, ex);
                }

                //summary.setSummaryData(null); // remove json before saving to database,

                try (DBHandler dbHandler = GBApplication.acquireDB()) {
                    DaoSession session = dbHandler.getDaoSession();
                    Device device = DBHelper.getDevice(getDevice(), session);
                    User user = DBHelper.getUser(session);
                    summary.setDevice(device);
                    summary.setUser(user);
                    session.getBaseActivitySummaryDao().insertOrReplace(summary);
                } catch (Exception ex) {
                    GB.toast(getContext(), "Error saving activity summary", Toast.LENGTH_LONG, GB.ERROR, ex);
                }

                LOG.info("Activity track:\n" + track.getSegments());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else { // We received a line of the csv, now we append it to the file in storage.
            // TODO: File manipulation adapted from onFetchRecordedData() - break out to a new function to avoid code duplication?

            File outputFile = new File(dir, filename);
            String filenameLogID = "latestFetchedRecorderLog.txt";
            File outputFileLogID = new File(dir, filenameLogID);
            LOG.warn("Writing log to " + outputFile.toString());
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
                writer.write(line);
                writer.close();
                //GB.toast(getContext(), "Log written to " + filename, Toast.LENGTH_LONG, GB.INFO);

                BufferedWriter writerLogID = new BufferedWriter(new FileWriter(outputFileLogID));
                writerLogID.write(log);
                writerLogID.close();
                //GB.toast(getContext(), "Log ID " + log + " written to " + filenameLogID, Toast.LENGTH_LONG, GB.INFO);
            } catch (IOException e) {
                LOG.warn("Could not write to file", e);
            }
        }
        if (json.getString("last").equals("true")) {
            getDevice().unsetBusyTask();
        }
    }

    /**
     * Handle "http" packet: make an HTTP request and return a "http" response
     */
    private void handleHttp(JSONObject json) throws JSONException {
        String _id = null;
        try {
            _id = json.getString("id");
        } catch (JSONException e) {
        }
        final String id = _id;

        if (! BuildConfig.INTERNET_ACCESS) {
            uartTxJSONError("http", "Internet access not enabled, check Gadgetbridge Device Settings", id);
            return;
        }

        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
        if (! devicePrefs.getBoolean(PREF_DEVICE_INTERNET_ACCESS, false)) {
            uartTxJSONError("http", "Internet access not enabled in this Gadgetbridge build", id);
            return;
        }

        String url = json.getString("url");

        int method = Request.Method.GET;
        if (json.has("method")) {
            String m = json.getString("method").toLowerCase();
            if (m.equals("get")) method = Request.Method.GET;
            else if (m.equals("post")) method = Request.Method.POST;
            else if (m.equals("head")) method = Request.Method.HEAD;
            else if (m.equals("put")) method = Request.Method.PUT;
            else if (m.equals("patch")) method = Request.Method.PATCH;
            else if (m.equals("delete")) method = Request.Method.DELETE;
            else uartTxJSONError("http", "Unknown HTTP method "+m,id);
        }

        byte[] _body = null;
        if (json.has("body"))
            _body = json.getString("body").getBytes();
        final byte[] body = _body;

        Map<String,String> _headers = null;
        if (json.has("headers")) {
            JSONObject h = json.getJSONObject("headers");
            _headers = new HashMap<String,String>();
            Iterator<String> iter = h.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    String value = h.getString(key);
                    _headers.put(key, value);
                } catch (JSONException e) {
                }
            }
        }
        final Map<String,String> headers = _headers;

        String _xmlPath = "";
        String _xmlReturn = "";
        try {
            _xmlPath = json.getString("xpath");
            _xmlReturn = json.getString("return");
        } catch (JSONException e) {
        }
        final String xmlPath = _xmlPath;
        final String xmlReturn = _xmlReturn;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(method, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject o = new JSONObject();
                        if (xmlPath.length() != 0) {
                            try {
                                InputSource inputXML = new InputSource(new StringReader(response));
                                XPath xPath = XPathFactory.newInstance().newXPath();
                                if (xmlReturn.equals("array")) {
                                    NodeList result = (NodeList) xPath.evaluate(xmlPath, inputXML, XPathConstants.NODESET);
                                    response = null; // don't add it below
                                    JSONArray arr = new JSONArray();
                                    if (result != null) {
                                        for (int i = 0; i < result.getLength(); i++)
                                            arr.put(result.item(i).getTextContent());
                                    }
                                    o.put("resp", arr);
                                } else {
                                    response = xPath.evaluate(xmlPath, inputXML);
                                }
                            } catch (Exception error) {
                                uartTxJSONError("http", error.toString(), id);
                                return;
                            }
                        }
                        try {
                            o.put("t", "http");
                            if( id!=null)
                                o.put("id", id);
                            if (response!=null)
                                o.put("resp", response);
                        } catch (JSONException e) {
                            GB.toast(getContext(), "HTTP: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
                        }
                        uartTxJSON("http", o);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                uartTxJSONError("http", error.toString(), id);
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                if (body == null) return super.getBody();
                return body;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // clone the data from super.getHeaders() so we can write to it
                Map<String, String> h = new HashMap<>(super.getHeaders());
                if (headers != null) {
                    for (String key : headers.keySet()) {
                        String value = headers.get(key);
                        h.put(key, value);
                    }
                }
                return h;
            }
        };
        RequestQueue queue = getRequestQueue();
        queue.add(stringRequest);
    }

    /**
     * Handle "force_calendar_sync" packet
     */
    private void handleCalendarSync(JSONObject json) throws JSONException {
        //if(!GBApplication.getPrefs().getBoolean("enable_calendar_sync", false)) return;
        //pretty much like the updateEvents in CalendarReceiver, but would need a lot of libraries here
        JSONArray ids = json.getJSONArray("ids");
        ArrayList<Long> idsList = new ArrayList<>(ids.length());
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            DaoSession session = dbHandler.getDaoSession();
            Long deviceId = DBHelper.getDevice(gbDevice, session).getId();
            QueryBuilder<CalendarSyncState> qb = session.getCalendarSyncStateDao().queryBuilder();
            //FIXME just use that and don't query every time?
            List<CalendarSyncState> states = qb.where(
                    CalendarSyncStateDao.Properties.DeviceId.eq(deviceId)).build().list();

            LOG.info("force_calendar_sync on banglejs: "+ ids.length() +" events on the device, "+ states.size() +" on our db");
            for (int i = 0; i < ids.length(); i++) {
                Long id = ids.getLong(i);
                qb = session.getCalendarSyncStateDao().queryBuilder(); //is this needed again?
                CalendarSyncState calendarSyncState = qb.where(
                        qb.and(CalendarSyncStateDao.Properties.DeviceId.eq(deviceId),
                                CalendarSyncStateDao.Properties.CalendarEntryId.eq(id))).build().unique();
                if(calendarSyncState == null) {
                    onDeleteCalendarEvent((byte)0, id);
                    LOG.info("event id="+ id +" is on device id="+ deviceId +", removing it there");
                } else {
                    //used for later, no need to check twice the ones that do not match
                    idsList.add(id);
                }
            }

            //remove all elements not in ids from database (we don't have them)
            for(CalendarSyncState calendarSyncState : states) {
                long id = calendarSyncState.getCalendarEntryId();
                if(!idsList.contains(id)) {
                    qb = session.getCalendarSyncStateDao().queryBuilder(); //is this needed again?
                    qb.where(qb.and(CalendarSyncStateDao.Properties.DeviceId.eq(deviceId),
                                    CalendarSyncStateDao.Properties.CalendarEntryId.eq(id)))
                            .buildDelete().executeDeleteWithoutDetachingEntities();
                    LOG.info("event id="+ id +" is not on device id="+ deviceId +", removing from our db");
                }
            }
        } catch (Exception e1) {
            GB.toast("Database Error while forcefully syncing Calendar", Toast.LENGTH_SHORT, GB.ERROR, e1);
        }
        //force a syncCalendar now, send missing events
        CalendarReceiver.forceSync();
    }

    /**
     * Handle "intent" packet: broadcast an Android intent
     */
    private void handleIntent(JSONObject json) throws JSONException {
        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
        if (!devicePrefs.getBoolean(PREF_DEVICE_INTENTS, false)) {
            uartTxJSONError("intent", "Android Intents not enabled, check Gadgetbridge Device Settings", null);
            return;
        }

        String target = json.has("target") ? json.getString("target") : "broadcastreceiver";
        Intent in = new Intent();
        if (json.has("action")) in.setAction(json.getString("action"));
        if (json.has("flags")) {
            JSONArray flags = json.getJSONArray("flags");
            for (int i = 0; i < flags.length(); i++) {
                in = addIntentFlag(in, flags.getString(i));
            }
        }
        if (json.has("categories")) {
            JSONArray categories = json.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                in.addCategory(categories.getString(i));
            }
        }
        if (json.has("package") && !json.has("class")) {
            in = json.getString("package").equals("gadgetbridge") ?
                    in.setPackage(this.getContext().getPackageName()) :
                    in.setPackage(json.getString("package"));
        }
        if (json.has("package") && json.has("class")) {
            in = json.getString("package").equals("gadgetbridge") ?
                    in.setClassName(this.getContext().getPackageName(), json.getString("class")) :
                    in.setClassName(json.getString("package"), json.getString("class"));
        }

        if (json.has("mimetype")) in.setType(json.getString("mimetype"));
        if (json.has("data")) in.setData(Uri.parse(json.getString("data")));
        if (json.has("extra")) {
            JSONObject extra = json.getJSONObject("extra");
            Iterator<String> iter = extra.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                in.putExtra(key, extra.getString(key)); // Should this be implemented for other types, e.g. extra.getInt(key)? Or will this always work even if receiving ints/doubles/etc.?
            }
        }
        LOG.info("Executing intent:\n\t" + String.valueOf(in) + "\n\tTargeting: " + target);
        //GB.toast(getContext(), String.valueOf(in), Toast.LENGTH_LONG, GB.INFO);
        switch (target) {
            case "broadcastreceiver":
                getContext().sendBroadcast(in);
                break;
            case "activity": // See wakeActivity.java if you want to start activities from under the keyguard/lock sceen.
                getContext().startActivity(in);
                break;
            case "service": // Should this be implemented differently, e.g. workManager?
                getContext().startService(in);
                break;
            case "foregroundservice": // Should this be implemented differently, e.g. workManager?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getContext().startForegroundService(in);
                } else {
                    getContext().startService(in);
                }
                break;
            default:
                LOG.info("Targeting '"+target+"' isn't implemented or doesn't exist.");
                GB.toast(getContext(), "Targeting '"+target+"' isn't implemented or it doesn't exist.", Toast.LENGTH_LONG, GB.INFO);
        }
    }

    private Intent addIntentFlag(Intent intent, String flag) {
        try {
            final Class<Intent> intentClass = Intent.class;
            final Field flagField = intentClass.getDeclaredField(flag);
            intent.addFlags(flagField.getInt(null));
        } catch (final Exception e) {
            // The user sent an invalid flag
            LOG.info("Flag '"+flag+"' isn't implemented or doesn't exist and was therefore not set.");
            GB.toast(getContext(), "Flag '"+flag+"' isn't implemented or it doesn't exist and was therefore not set.", Toast.LENGTH_LONG, GB.INFO);
        }
        return intent;
    }

    @Override
    public void onSendConfiguration(final String config) {
        switch (config) {
            case PREF_BANGLEJS_ACTIVITY_FULL_SYNC_START:
                fetchActivityData(0);
                return;
        }

        LOG.warn("Unknown config changed: {}", config);
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }
        if (BangleJSConstants.UUID_CHARACTERISTIC_NORDIC_UART_RX.equals(characteristic.getUuid())) {
            byte[] chars = characteristic.getValue();
            // check to see if we get more data - if so, increase out MTU for sending
            if (allowHighMTU && chars.length > mtuSize)
                mtuSize = chars.length;
            // Scan for flow control characters
            for (int i=0;i<chars.length;i++) {
                boolean ignoreChar = false;
                if (chars[i]==19 /* XOFF */) {
                    getQueue().setPaused(true);
                    LOG.info("RX: XOFF");
                    ignoreChar = true;
                }
                if (chars[i]==17 /* XON */) {
                    getQueue().setPaused(false);
                    LOG.info("RX: XON");
                    ignoreChar = true;
                }
                if (ignoreChar) {
                    // remove char from the array. Generally only one XON/XOFF per stream so creating a new array each time is fine
                    byte[] c = new byte[chars.length - 1];
                    System.arraycopy(chars, 0, c, 0, i); // copy before
                    System.arraycopy(chars, i+1, c, i, chars.length - i - 1); // copy after
                    chars = c;
                    i--; // back up one (because we deleted it)
                }
            }
            String packetStr = new String(chars, StandardCharsets.ISO_8859_1);
            LOG.debug("RX: " + packetStr);
            // logging
            addReceiveHistory(packetStr);
            // split into input lines
            receivedLine += packetStr;
            while (receivedLine.contains("\n")) {
                int p = receivedLine.indexOf("\n");
                String line = receivedLine.substring(0,(p>0) ? (p-1) : 0);
                receivedLine = receivedLine.substring(p+1);
                handleUartRxLine(line);
            }
            // Send an intent with new data
            Intent intent = new Intent(BangleJSDeviceSupport.BANGLEJS_COMMAND_RX);
            intent.putExtra("DATA", packetStr);
            intent.putExtra("SEQ", bangleCommandSeq++);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
        return false;
    }


    void transmitTime(TransactionBuilder builder) {
      long ts = System.currentTimeMillis();
      float tz = SimpleTimeZone.getDefault().getOffset(ts) / (1000 * 60 * 60.0f);
      // set time
      String cmd = "\u0010setTime("+(ts/1000)+");";
      // set timezone
      cmd += "E.setTimeZone("+tz+");";
      // write timezone to settings
      cmd += "(s=>s&&(s.timezone="+tz+",require('Storage').write('setting.json',s)))(require('Storage').readJSON('setting.json',1))";
      uartTx(builder, cmd+"\n");
    }

    void requestBangleGPSPowerStatus() {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "is_gps_active");
            LOG.debug("Requesting gps power status: " + o.toString());
            uartTxJSON("is_gps_active", o);
        } catch (JSONException e) {
            GB.toast(getContext(), "uartTxJSONError: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }

    void setupGPSUpdateTimer() {
        if (gpsUpdateSetup) {
            LOG.debug("GPS position timer is already setup");
            return;
        }
        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
        if(devicePrefs.getBoolean(PREF_DEVICE_GPS_UPDATE, false)) {
            int intervalLength = devicePrefs.getInt(PREF_DEVICE_GPS_UPDATE_INTERVAL, 1000);
            LOG.info("Setup location listener with an update interval of " + intervalLength + " ms");
            boolean onlyUseNetworkGPS = devicePrefs.getBoolean(PREF_DEVICE_GPS_USE_NETWORK_ONLY, false);
            LOG.info("Using combined GPS and NETWORK based location: " + onlyUseNetworkGPS);
            if (!onlyUseNetworkGPS) {
                try {
                    GBLocationManager.start(getContext(), this, LocationProviderType.GPS, intervalLength);
                } catch (IllegalArgumentException e) {
                    LOG.warn("GPS provider could not be started", e);
                }
            }

            try {
                GBLocationManager.start(getContext(), this, LocationProviderType.NETWORK, intervalLength);
            } catch (IllegalArgumentException e) {
                LOG.warn("NETWORK provider could not be started", e);
            }
        } else {
            GB.toast("Phone gps data update is deactivated in the settings", Toast.LENGTH_SHORT, GB.INFO);
        }
        gpsUpdateSetup = true;
    }

    @Override
    public void onSetGpsLocation(final Location location) {
        if (!GBApplication.getPrefs().getBoolean("use_updated_location_if_available", false)) return;
        LOG.debug("new location: " + location.toString());
        JSONObject o = new JSONObject();
        try {
            o.put("t", "gps");
            o.put("lat", location.getLatitude());
            o.put("lon", location.getLongitude());
            o.put("alt", location.getAltitude());
            o.put("speed", location.getSpeed()*3.6); // m/s to kph
            if (location.hasBearing()) o.put("course", location.getBearing());
            o.put("time", location.getTime());
            if (location.getExtras() != null) {
                LOG.debug("Found number of satellites: " + location.getExtras().getInt("satellites", -1));
                o.put("satellites",location.getExtras().getInt("satellites"));
            } else {
                o.put("satellites", 0);
            }
            o.put("hdop", location.getAccuracy());
            o.put("externalSource", true);
            o.put("gpsSource", location.getProvider());
            LOG.debug("Sending gps value: " + o.toString());
            uartTxJSON("gps", o);
        } catch (JSONException e) {
            GB.toast(getContext(), "uartTxJSONError: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }



    @Override
    public boolean useAutoConnect() {
        return true;
    }

    private String renderUnicodeWordPartAsImage(String word) {
        // check for emoji
        boolean hasEmoji = false;
        if (EmojiUtils.getAllEmojis() == null)
            EmojiManager.initEmojiData(GBApplication.getContext());
        for (Emoji emoji : EmojiUtils.getAllEmojis())
            if (word.contains(emoji.getEmoji())) {
                hasEmoji = true;
                break;
            }
        // if we had emoji, ensure we create 3 bit color (not 1 bit B&W)
        BangleJSBitmapStyle style = hasEmoji ? BangleJSBitmapStyle.RGB_3BPP_TRANSPARENT : BangleJSBitmapStyle.MONOCHROME_TRANSPARENT;
        return "\0"+bitmapToEspruinoString(textToBitmap(word), style);
    }

    private String renderUnicodeWordAsImage(String word) {
        // if we have Chinese/Japanese/Korean chars, split into 2 char chunks to allow easier text wrapping
        // it's not perfect but better than nothing
        boolean hasCJK = false;
        for (int i=0;i<word.length();i++) {
            char ch = word.charAt(i);
            hasCJK |= ch>=0x4E00 && ch<=0x9FFF; // "CJK Unified Ideographs" block
        }
        if (hasCJK) {
            // split every 2 chars
            StringBuilder result = new StringBuilder();
            for (int i=0;i<word.length();i+=2) {
                int len = 2;
                if (i+len > word.length())
                    len = word.length()-i;
                result.append(renderUnicodeWordPartAsImage(word.substring(i, i + len)));
            }
            return result.toString();
        }
        // else just render the word as-is
        return renderUnicodeWordPartAsImage(word);
    }

    public String renderUnicodeAsImage(String txt) {
        // FIXME: it looks like we could implement this as customStringFilter now so it happens automatically
        if (txt==null) return null;
        // Simple conversions
        txt = txt.replaceAll("…", "...");
        /* If we're not doing conversion, pass this right back (we use the EmojiConverter
        As we would have done if BangleJSCoordinator.supportsUnicodeEmojis had reported false */
        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
        if (!devicePrefs.getBoolean(PREF_BANGLEJS_TEXT_BITMAP, false))
            return EmojiConverter.convertUnicodeEmojiToAscii(txt, GBApplication.getContext());
         // Otherwise split up and check each word
        String word = "";
        StringBuilder result = new StringBuilder();
        boolean needsTranslate = false;
        for (int i=0;i<txt.length();i++) {
            char ch = txt.charAt(i);
            // Special cases where we can just use a built-in character...
            // Based on https://op.europa.eu/en/web/eu-vocabularies/formex/physical-specifications/character-encoding
            if (ch=='–' || ch=='‐' || ch=='—') ch='-';
            else if (ch =='‚' || ch=='，' || ch=='、') ch=',';
            else if (ch =='。') ch='.';
            else if (ch =='【') ch='[';
            else if (ch =='】') ch=']';
            else if (ch=='‘' || ch=='’' || ch=='‛' || ch=='′' || ch=='ʹ') ch='\'';
            else if (ch=='“' || ch=='”' || ch =='„' || ch=='‟' || ch=='″') ch='"';
            // chars which break words up
            if (" -_/:.,?!'\"&*()[]".indexOf(ch)>=0) {
                // word split
                if (needsTranslate) { // convert word
                    LOG.info("renderUnicodeAsImage converting " + word);
                    result.append(renderUnicodeWordAsImage(word)).append(ch);
                } else { // or just copy across
                    result.append(word).append(ch);
                }
                word = "";
                needsTranslate = false;
            } else {
                // TODO: better check?
                if (ch>255) needsTranslate = true;
                word += ch;
            }
        }
        if (needsTranslate) { // convert word
            LOG.info("renderUnicodeAsImage converting " + word);
            result.append(renderUnicodeWordAsImage(word));
        } else { // or just copy across
            result.append(word);
        }
        return result.toString();
    }

    /// Crop a text string to ensure it's not longer than requested
    public String cropToLength(String txt, int len) {
        if (txt==null) return "";
        if (txt.length()<=len) return txt;
        return txt.substring(0,len-3)+"...";
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        if (notificationSpec.attachedActions!=null)
            for (int i=0;i<notificationSpec.attachedActions.size();i++) {
                NotificationSpec.Action action = notificationSpec.attachedActions.get(i);
                if (action.type==NotificationSpec.Action.TYPE_WEARABLE_REPLY)
                    mNotificationReplyAction.add(notificationSpec.getId(), ((long) notificationSpec.getId() << 4) + i + 1);
            }
        // sourceName isn't set for SMS messages
        String src = notificationSpec.sourceName;
        if (notificationSpec.type == NotificationType.GENERIC_SMS)
            src = "SMS Message";
        // Send JSON to Bangle.js
        try {
            JSONObject o = new JSONObject();
            o.put("t", "notify");
            o.put("id", notificationSpec.getId());
            o.put("src", src);
            o.put("title", renderUnicodeAsImage(cropToLength(notificationSpec.title,80)));
            o.put("subject", renderUnicodeAsImage(cropToLength(notificationSpec.subject,80)));
            o.put("body", renderUnicodeAsImage(cropToLength(notificationSpec.body, 400)));
            o.put("sender", renderUnicodeAsImage(cropToLength(notificationSpec.sender,40)));
            o.put("tel", notificationSpec.phoneNumber);
            uartTxJSON("onNotification", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onDeleteNotification(int id) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "notify-");
            o.put("id", id);
            uartTxJSON("onDeleteNotification", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetTime() {
        try {
            TransactionBuilder builder = performInitialized("setTime");
            transmitTime(builder);
            //TODO: once we have a common strategy for sending events (e.g. EventHandler), remove this call from here. Meanwhile it does no harm.
            // = we should generalize the pebble calender code
            forceCalendarSync();
            builder.queue(getQueue());
        } catch (Exception e) {
            GB.toast(getContext(), "Error setting time: " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "alarm");
            JSONArray jsonalarms = new JSONArray();
            o.put("d", jsonalarms);

            for (Alarm alarm : alarms) {
                if (alarm.getUnused()) continue;
                JSONObject jsonalarm = new JSONObject();
                jsonalarms.put(jsonalarm);
                //Calendar calendar = AlarmUtils.toCalendar(alarm);
                jsonalarm.put("h", alarm.getHour());
                jsonalarm.put("m", alarm.getMinute());
                jsonalarm.put("rep", alarm.getRepetition());
                jsonalarm.put("on", alarm.getEnabled());
            }
            uartTxJSON("onSetAlarms", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetCallState(CallSpec callSpec) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "call");
            String cmdName = "";
            try {
                Field[] fields = callSpec.getClass().getDeclaredFields();
                for (Field field : fields)
                    if (field.getName().startsWith("CALL_") && field.getInt(callSpec) == callSpec.command)
                        cmdName = field.getName().substring(5).toLowerCase();
            } catch (IllegalAccessException e) {}
            o.put("cmd", cmdName);
            o.put("name", renderUnicodeAsImage(callSpec.name));
            o.put("number", callSpec.number);
            uartTxJSON("onSetCallState", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "musicstate");
            int musicState = stateSpec.state;
            String[] musicStates = {"play", "pause", "stop", ""};
            if (musicState<0) musicState=3;
            if (musicState>=musicStates.length) musicState = musicStates.length-1;
            o.put("state", musicStates[musicState]);
            o.put("position", stateSpec.position);
            o.put("shuffle", stateSpec.shuffle);
            o.put("repeat", stateSpec.repeat);
            uartTxJSON("onSetMusicState", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "musicinfo");
            o.put("artist", renderUnicodeAsImage(musicSpec.artist));
            o.put("album", renderUnicodeAsImage(musicSpec.album));
            o.put("track", renderUnicodeAsImage(musicSpec.track));
            o.put("dur", musicSpec.duration);
            o.put("c", musicSpec.trackCount);
            o.put("n", musicSpec.trackNr);
            uartTxJSON("onSetMusicInfo", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    private void transmitActivityStatus() {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "act");
            o.put("hrm", realtimeHRM);
            o.put("stp", realtimeStep);
            o.put("int", realtimeHRMInterval);
            uartTxJSON("onEnableRealtimeSteps", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    private void requestActivityTracksList(String lastSyncedID) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "listRecs");
            o.put("id", lastSyncedID);
            uartTxJSON("requestActivityTracksList", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    private void requestActivityTrackLog(String id, Boolean isLastId) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "fetchRec");
            o.put("id", id);
            o.put("last", String.valueOf(isLastId));
            uartTxJSON("requestActivityTrackLog", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        if (enable == realtimeHRM) return;
        realtimeStep = enable;
        transmitActivityStatus();
    }

    @Override
    public void onFetchRecordedData(int dataTypes) {
        if ((dataTypes & RecordedDataTypes.TYPE_ACTIVITY) != 0)  {
            fetchActivityData(getLastSuccessfulSyncTime());
        }

        if (dataTypes == RecordedDataTypes.TYPE_GPS_TRACKS) {
            getDevice().setBusyTask("Fetch Activity Tracks");
            GB.toast("TYPE_GPS_TRACKS says hi!", Toast.LENGTH_LONG, GB.INFO);
            File dir;
            try {
                dir = FileUtils.getExternalFilesDir();
            } catch (IOException e) {
                return;
            }
            String filename = "latestFetchedRecorderLog.txt";
            File inputFile = new File(dir, filename);
            BufferedReader reader;
            String lastSyncedID = "";
            try {
                reader = new BufferedReader(new FileReader(inputFile));
                lastSyncedID = String.valueOf(reader.readLine());
                reader.close();
            } catch (IOException ignored) {
            }
            //lastSyncedID = "20230706x"; // DEBUGGING

            LOG.info("Last Synced log ID: " + lastSyncedID);
            requestActivityTracksList(lastSyncedID);
        }
        if (dataTypes == RecordedDataTypes.TYPE_DEBUGLOGS) {
            File dir;
            try {
                dir = FileUtils.getExternalFilesDir();
            } catch (IOException e) {
                return;
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
            String filename = "banglejs_debug_" + dateFormat.format(new Date()) + ".log";
            File outputFile = new File(dir, filename);
            LOG.warn("Writing log to "+outputFile.toString());
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                writer.write(receiveHistory);
                writer.close();
                receiveHistory = "";
                GB.toast(getContext(), "Log written to "+filename, Toast.LENGTH_LONG, GB.INFO);
            } catch (IOException e) {
                LOG.warn("Could not write to file", e);
            }
        }
    }

    protected void fetchActivityData(final long timestampMillis) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "actfetch");
            o.put("ts", timestampMillis);
            uartTxJSON("fetch activity data", o);
        } catch (final JSONException e) {
            LOG.warn("Failed to fetch activity data", e);
        }
    }

    protected String getLastSyncTimeKey() {
        return "lastSyncTimeMillis";
    }

    protected void saveLastSyncTimestamp(final long timestamp) {
        final SharedPreferences.Editor editor = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()).edit();
        editor.putLong(getLastSyncTimeKey(), timestamp);
        editor.apply();
    }

    protected long getLastSuccessfulSyncTime() {
        long timeStampMillis = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()).getLong(getLastSyncTimeKey(), 0);
        if (timeStampMillis != 0) {
            return timeStampMillis;
        }
        final GregorianCalendar calendar = BLETypeConversions.createCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTimeInMillis();
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        if (enable == realtimeHRM) return;
        realtimeHRM = enable;
        transmitActivityStatus();
    }

    @Override
    public void onFindDevice(boolean start) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "find");
            o.put("n", start);
            uartTxJSON("onFindDevice", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetConstantVibration(int integer) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "vibrate");
            o.put("n", integer);
            uartTxJSON("onSetConstantVibration", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {
        realtimeHRMInterval = seconds;
        transmitActivityStatus();
    }

    private List<LoyaltyCard> filterSupportedCards(final List<LoyaltyCard> cards) {
        final List<LoyaltyCard> ret = new ArrayList<>();
        for (final LoyaltyCard card : cards) {
            // we hardcode here what is supported
            if (card.getBarcodeFormat() == BarcodeFormat.CODE_39 ||
                    card.getBarcodeFormat() == BarcodeFormat.CODABAR ||
                    card.getBarcodeFormat() == BarcodeFormat.QR_CODE) {
                ret.add(card);
            }
        }
        return ret;
    }

    @Override
    public void onSetLoyaltyCards(final ArrayList<LoyaltyCard> cards) {
        final List<LoyaltyCard> supportedCards = filterSupportedCards(cards);
        try {
            JSONObject encoded_cards = new JSONObject();
            JSONArray a = new JSONArray();
            for (final LoyaltyCard card : supportedCards) {
                JSONObject o = new JSONObject();
                o.put("id", card.getId());
                o.put("name", renderUnicodeAsImage(cropToLength(card.getName(),40)));
                if (card.getBarcodeId() != null) {
                    o.put("value", card.getBarcodeId());
                } else {
                    o.put("value", card.getCardId());
                }
                if (card.getBarcodeFormat() != null)
                    o.put("type", card.getBarcodeFormat().toString());
                if (card.getExpiry() != null)
                    o.put("expiration", card.getExpiry().getTime()/1000);
                o.put("color", card.getColor());
                // we somehow cannot distinguish no balance defined with 0 P
                if (card.getBalance() != null && card.getBalance().signum() != 0
                        || card.getBalanceType() != null) {
                    // if currency is points it is not reported
                    String balanceType = card.getBalanceType() != null ?
                        card.getBalanceType().toString() : "P";
                    o.put("balance", renderUnicodeAsImage(cropToLength(card.getBalance() +
                                    " " + balanceType, 20)));
                }
                if (card.getNote() != null)
                    o.put("note", renderUnicodeAsImage(cropToLength(card.getNote(),200)));
                a.put(o);
            }
            encoded_cards.put("t", "cards");
            encoded_cards.put("d", a);
            uartTxJSON("onSetLoyaltyCards", encoded_cards);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "calendar");
            o.put("id", calendarEventSpec.id);
            o.put("type", calendarEventSpec.type); //implement this too? (sunrise and set)
            o.put("timestamp", calendarEventSpec.timestamp);
            o.put("durationInSeconds", calendarEventSpec.durationInSeconds);
            o.put("title", renderUnicodeAsImage(cropToLength(calendarEventSpec.title,40)));
            o.put("description", renderUnicodeAsImage(cropToLength(calendarEventSpec.description,200)));
            o.put("location", renderUnicodeAsImage(cropToLength(calendarEventSpec.location,40)));
            o.put("calName", cropToLength(calendarEventSpec.calName,20));
            o.put("color", calendarEventSpec.color);
            o.put("allDay", calendarEventSpec.allDay);
            uartTxJSON("onAddCalendarEvent", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "calendar-");
            o.put("id", id);
            uartTxJSON("onDeleteCalendarEvent", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "weather");
            o.put("temp", weatherSpec.currentTemp);
            o.put("hi", weatherSpec.todayMaxTemp);
            o.put("lo", weatherSpec.todayMinTemp );
            o.put("hum", weatherSpec.currentHumidity);
            o.put("rain", weatherSpec.precipProbability);
            o.put("uv", Math.round(weatherSpec.uvIndex*10)/10);
            o.put("code", weatherSpec.currentConditionCode);
            o.put("txt", weatherSpec.currentCondition);
            o.put("wind", Math.round(weatherSpec.windSpeed*100)/100.0);
            o.put("wdir", weatherSpec.windDirection);
            o.put("loc", weatherSpec.location);
            uartTxJSON("onSendWeather", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    public Bitmap textToBitmap(String text) {
        Paint paint = new Paint(0); // Paint.ANTI_ALIAS_FLAG not wanted as 1bpp
        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
        paint.setTextSize(devicePrefs.getInt(PREF_BANGLEJS_TEXT_BITMAP_SIZE, 18));
        paint.setColor(0xFFFFFFFF);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        if (width<1) width=1;
        if (height<1) height=1;
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public enum BangleJSBitmapStyle {
        MONOCHROME, // 1bpp
        MONOCHROME_TRANSPARENT, // 1bpp, black = transparent
        RGB_3BPP, // 3bpp
        RGB_3BPP_TRANSPARENT // 3bpp, least used color as transparent
    }

    /** Used for writing single bits to an array */
    public static class BitWriter {
        int n;
        final byte[] bits;
        int currentByte, bitIdx;

        public BitWriter(byte[] array, int offset) {
            bits = array;
            n = offset;
        }

        public void push(boolean v) {
            currentByte = (currentByte << 1) | (v?1:0);
            bitIdx++;
            if (bitIdx == 8) {
                bits[n++] = (byte)currentByte;
                bitIdx = 0;
                currentByte = 0;
            }
        }

        public void finish() {
            if (bitIdx > 0) bits[n++] = (byte)currentByte;
        }
    }

    /** Convert an Android bitmap to a base64 string for use in Espruino.
     * Currently only 1bpp, no scaling */
    public static byte[] bitmapToEspruinoArray(Bitmap bitmap, BangleJSBitmapStyle style) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width>255) {
            LOG.warn("bitmapToEspruinoArray width of "+width+" > 255 (Espruino max) - cropping");
            width = 255;
        }
        if (height>255) {
            LOG.warn("bitmapToEspruinoArray height of "+height+" > 255 (Espruino max) - cropping");
            height = 255;
        }
        int bpp = (style==BangleJSBitmapStyle.RGB_3BPP ||
                   style==BangleJSBitmapStyle.RGB_3BPP_TRANSPARENT) ? 3 : 1;
        byte[] pixels = new byte[width * height];
        final byte PIXELCOL_TRANSPARENT = -1;
        final int[] ditherMatrix = {1*16,5*16,7*16,3*16}; // for bayer dithering
        // if doing RGB_3BPP_TRANSPARENT, check image to see if it's transparent
        // MONOCHROME_TRANSPARENT is handled later on...
        boolean allowTransparency = (style == BangleJSBitmapStyle.RGB_3BPP_TRANSPARENT);
        boolean isTransparent = false;
        byte transparentColorIndex = 0;
        /* Work out what colour index each pixel should be and write to pixels.
         Also figure out if we're transparent at all, and how often each color is used */
        int[] colUsage = new int[8];
        int n = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = pixel & 255;
                int g = (pixel >> 8) & 255;
                int b = (pixel >> 16) & 255;
                int a = (pixel >> 24) & 255;
                boolean pixelTransparent = allowTransparency && (a < 128);
                if (pixelTransparent) {
                    isTransparent = true;
                    r = g = b = 0;
                }
                // do dithering here
                int ditherAmt = ditherMatrix[(x&1) + (y&1)*2];
                r += ditherAmt;
                g += ditherAmt;
                b += ditherAmt;
                int col = 0;
                if (bpp==1)
                    col = ((r+g+b) >= 768)?1:0;
                else if (bpp==3)
                    col = ((r>=256)?1:0) | ((g>=256)?2:0) | ((b>=256)?4:0);
                if (!pixelTransparent) colUsage[col]++; // if not transparent, record usage
                // save colour, mark transparent separately
                pixels[n++] = (byte)(pixelTransparent ? PIXELCOL_TRANSPARENT : col);
            }
        }
        // if we're transparent, find the least-used color, and use that for transparency
        if (isTransparent) {
            // find least used
            int minColUsage = -1;
            for (int c=0;c<8;c++) {
                if (minColUsage<0 || colUsage[c]<minColUsage) {
                    minColUsage = colUsage[c];
                    transparentColorIndex = (byte)c;
                }
            }
            // rewrite any transparent pixels as the correct color for transparency
            for (n=0;n<pixels.length;n++)
                if (pixels[n]==PIXELCOL_TRANSPARENT)
                    pixels[n] = transparentColorIndex;
        }
        // if we're MONOCHROME_TRANSPARENT, force transparency on bg color
        if (style == BangleJSBitmapStyle.MONOCHROME_TRANSPARENT) {
            isTransparent = true;
            transparentColorIndex = 0;
        }
        // Write the header
        int headerLen = isTransparent ? 4 : 3;
        byte[] bmp = new byte[(((height * width * bpp) + 7) >> 3) + headerLen];
        bmp[0] = (byte)width;
        bmp[1] = (byte)height;
        bmp[2] = (byte)(bpp + (isTransparent?128:0));
        if (isTransparent) bmp[3] = transparentColorIndex;
        // Now write the image out bit by bit
        BitWriter bits = new BitWriter(bmp, headerLen);
        n = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[n++];
                for (int b=bpp-1;b>=0;b--)
                    bits.push(((pixel>>b)&1) != 0);
            }
        }
        bits.finish();
        return bmp;
    }

    /** Convert an Android bitmap to a base64 string for use in Espruino.
     * Currently only 1bpp, no scaling */
    public static String bitmapToEspruinoString(Bitmap bitmap, BangleJSBitmapStyle style) {
        return new String(bitmapToEspruinoArray(bitmap, style), StandardCharsets.ISO_8859_1);
    }

    /** Convert an Android bitmap to a base64 string for use in Espruino.
     * Currently only 1bpp, no scaling */
    public static String bitmapToEspruinoBase64(Bitmap bitmap, BangleJSBitmapStyle style) {
        return Base64.encodeToString(bitmapToEspruinoArray(bitmap, style), Base64.DEFAULT).replaceAll("\n","");
    }

    /** Convert a drawable to a bitmap, for use with bitmapToEspruino */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        final int maxWidth = 32;
        final int maxHeight = 32;
        /* Return bitmap directly but only if it's small enough. It could be
        we have a bitmap but it's just too big to send direct to the bangle */
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bmp = bitmapDrawable.getBitmap();
            if (bmp != null && bmp.getWidth()<=maxWidth && bmp.getHeight()<=maxHeight)
                return bmp;
        }
        /* Otherwise render this to a bitmap ourselves.. work out size */
        int w = maxWidth;
        int h = maxHeight;
        if (drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
            w = drawable.getIntrinsicWidth();
            h = drawable.getIntrinsicHeight();
            // don't allocate anything too big, but keep the ratio
            if (w>maxWidth) {
                h = h * maxWidth / w;
                w = maxWidth;
            }
            if (h>maxHeight) {
                w = w * maxHeight / h;
                h = maxHeight;
            }
        }
        /* render */
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /*
     * Request the banglejs to send all ids to sync with our database
     * TODO perhaps implement a minimum interval between consecutive requests
     */
    private void forceCalendarSync() {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "force_calendar_sync_start");
            uartTxJSON("forceCalendarSync", o);
        } catch(JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void onSetNavigationInfo(NavigationInfoSpec navigationInfoSpec) {
        try {
            JSONObject o = new JSONObject();
            o.put("t", "nav");
            if (navigationInfoSpec.instruction!=null)
                o.put("instr", navigationInfoSpec.instruction);
            o.put("distance", navigationInfoSpec.distanceToTurn);
            String[] navActions = {
                    "","continue", "left", "left_slight", "left_sharp",  "right", "right_slight",
                    "right_sharp", "keep_left", "keep_right", "uturn_left", "uturn_right",
                    "offroute", "roundabout_right", "roundabout_left", "roundabout_straight", "roundabout_uturn", "finish"};
            if (navigationInfoSpec.nextAction>0 && navigationInfoSpec.nextAction<navActions.length)
                o.put("action", navActions[navigationInfoSpec.nextAction]);
            if (navigationInfoSpec.ETA!=null)
                o.put("eta", navigationInfoSpec.ETA);
            uartTxJSON("onSetNavigationInfo", o);
        } catch (JSONException e) {
            LOG.info("JSONException: " + e.getLocalizedMessage());
        }
    }

    private ActivityTrackExporter createExporter() {
        GPXExporter exporter = new GPXExporter();
        exporter.setCreator(GBApplication.app().getNameAndVersion());
        return exporter;
    }

    protected JSONObject addSummaryData(JSONObject summaryData, String key, float value, String unit) {
        if (value > 0) {
            try {
                JSONObject innerData = new JSONObject();
                innerData.put("value", value);
                innerData.put("unit", unit);
                summaryData.put(key, innerData);
            } catch (JSONException ignore) {
            }
        }
        return summaryData;
    }

   // protected JSONObject addSummaryData(JSONObject summaryData, String key, String value) {
   //     if (key != null && !key.equals("") && value != null && !value.equals("")) {
   //         try {
   //             JSONObject innerData = new JSONObject();
   //             innerData.put("value", value);
   //             innerData.put("unit", "string");
   //             summaryData.put(key, innerData);
   //         } catch (JSONException ignore) {
   //         }
   //     }
   //     return summaryData;
   // }

    private String distanceFromCoordinatePairs(String latA, String lonA, String latB, String lonB) {
        // https://en.wikipedia.org/wiki/Geographic_coordinate_system#Length_of_a_degree
        //phi = latitude
        //lambda = longitude
        //length of 1 degree lat:
        //111132.92 - 559.82*cos(2*phi) + 1.175*cos(4*phi) - 0.0023*cos(6*phi)
        //length of 1 degree lon:
        //111412.84*cos(phi) - 93.5*cos(3*phi) + 0.118*cos(5*phi)
        double latADouble = Double.parseDouble(latA);
        double latBDouble = Double.parseDouble(latB);
        double lonADouble = Double.parseDouble(lonA);
        double lonBDouble = Double.parseDouble(lonB);

        double lengthPerDegreeLat = 111132.92 - 559.82*cos(2*latADouble) + 1.175*cos(4*latADouble) - 0.0023*cos(6*latADouble);
        double lengthPerDegreeLon = 111412.84*cos(latADouble) - 93.5*cos(3*latADouble) + 0.118*cos(5*latADouble);

        double latDist = (latBDouble-latADouble)*lengthPerDegreeLat;
        double lonDist = (lonBDouble-lonADouble)*lengthPerDegreeLon;

        return String.valueOf(sqrt(latDist*latDist+lonDist*lonDist));
    }

    private float sumOfJSONArray(JSONArray a) throws JSONException {
        double sum = 0;
        for (int i=0; i<a.length(); i++) {
            sum += a.getDouble(i);
        }
        return (float) sum;
    }

    private float averageOfJSONArray(JSONArray a) throws JSONException {
        return sumOfJSONArray(a) / a.length();
    }

    private float minOfJSONArray(JSONArray a) throws JSONException {
        double min = a.getDouble(0);
        for (int i=1; i<a.length(); i++) {
            min = Math.min(min, a.getDouble(i));
        }
        return (float) min;
    }

    private float maxOfJSONArray(JSONArray a) throws JSONException {
        double max = a.getDouble(0);
        for (int i=1; i<a.length(); i++) {
            max = Math.max(max, a.getDouble(i));
        }
        return (float) max;
    }
}
