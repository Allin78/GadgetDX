/*  Copyright (C) 2015-2021 Andreas Böhler, Andreas Shimokawa, Carsten
    Pfeiffer, Daniel Dakhno, Daniele Gobbetti, JohnnySun, José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.btle;

import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DEVICE_INTENTS;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.Logging;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Reminder;
import nodomain.freeyourgadget.gadgetbridge.model.WorldClock;
import nodomain.freeyourgadget.gadgetbridge.service.AbstractDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.CheckInitializedAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.AbstractBleProfile;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

/**
 * Abstract base class for all devices connected through Bluetooth Low Energy (LE) aka
 * Bluetooth Smart.
 * <p/>
 * The connection to the device and all communication is made with a generic {@link BtLEQueue}.
 * Messages to the device are encoded as {@link BtLEAction actions} or {@link BtLEServerAction actions}
 * that are grouped with a {@link Transaction} or {@link ServerTransaction} and sent via {@link BtLEQueue}.
 *
 * @see TransactionBuilder
 * @see BtLEQueue
 */
public abstract class AbstractBTLEDeviceSupport extends AbstractDeviceSupport implements GattCallback, GattServerCallback {
    private BtLEQueue mQueue;
    private Map<UUID, BluetoothGattCharacteristic> mAvailableCharacteristics;
    private final Set<UUID> mSupportedServices = new HashSet<>(4);
    private final Set<BluetoothGattService> mSupportedServerServices = new HashSet<>(4);
    private Logger logger;

     private int mtuSize;

    private final List<AbstractBleProfile<?>> mSupportedProfiles = new ArrayList<>();
    public static final String BASE_UUID = "0000%s-0000-1000-8000-00805f9b34fb"; //this is common for all BTLE devices. see http://stackoverflow.com/questions/18699251/finding-out-android-bluetooth-le-gatt-profiles
    private final Object characteristicsMonitor = new Object();

    private BluetoothGattCharacteristic rxCharacteristic = null;
    private BluetoothGattCharacteristic txCharacteristic = null;

    private String receiveHistory = "";

    /// Maximum amount of characters to store in receiveHistory
    public static final int MAX_RECEIVE_HISTORY_CHARS = 100000;

    public AbstractBTLEDeviceSupport(Logger logger) {
        this.logger = logger;
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null");
        }
    }

    @Override
    public boolean connect() {
        if (mQueue == null) {
            mQueue = new BtLEQueue(getBluetoothAdapter(), getDevice(), this, this, getContext(), mSupportedServerServices);
            mQueue.setAutoReconnect(getAutoReconnect());
            mQueue.setImplicitGattCallbackModify(getImplicitCallbackModify());
        }
        return mQueue.connect();
    }

    @Override
    public void setAutoReconnect(boolean enable) {
        super.setAutoReconnect(enable);
        if (mQueue != null) {
            mQueue.setAutoReconnect(enable);
        }
    }

    /**
     * Subclasses should populate the given builder to initialize the device (if necessary).
     *
     * @param builder
     * @return the same builder as passed as the argument
     */
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        return builder;
    }

    @Override
    public void dispose() {
        if (mQueue != null) {
            mQueue.dispose();
            mQueue = null;
        }
    }

    public TransactionBuilder createTransactionBuilder(String taskName) {
        return new TransactionBuilder(taskName);
    }

    /**
     * Send commands like this to the device:
     * <p>
     * <code>performInitialized("sms notification").write(someCharacteristic, someByteArray).queue(getQueue());</code>
     * </p>
     * This will asynchronously
     * <ul>
     * <li>connect to the device (if necessary)</li>
     * <li>initialize the device (if necessary)</li>
     * <li>execute the commands collected with the returned transaction builder</li>
     * </ul>
     *
     * @see #performConnected(Transaction)
     * @see #initializeDevice(TransactionBuilder)
     */
    public TransactionBuilder performInitialized(String taskName) throws IOException {
        if (!isConnected()) {
            if (!connect()) {
                throw new IOException("1: Unable to connect to device: " + getDevice());
            }
        }
        if (!isInitialized()) {
            // first, add a transaction that performs device initialization
            TransactionBuilder builder = createTransactionBuilder("Initialize device");
            builder.add(new CheckInitializedAction(gbDevice));
            initializeDevice(builder).queue(getQueue());
        }
        return createTransactionBuilder(taskName);
    }

    public ServerTransactionBuilder createServerTransactionBuilder(String taskName) {
        return new ServerTransactionBuilder(taskName);
    }

    public ServerTransactionBuilder performServer(String taskName) throws IOException {
        if (!isConnected()) {
            if(!connect()) {
                throw new IOException("1: Unable to connect to device: " + getDevice());
            }
        }
        return createServerTransactionBuilder(taskName);
    }

    /**
     * Ensures that the device is connected and (only then) performs the actions of the given
     * transaction builder.
     *
     * In contrast to {@link #performInitialized(String)}, no initialization sequence is performed
     * with the device, only the actions of the given builder are executed.
     * @param transaction
     * @throws IOException
     * @see {@link #performInitialized(String)}
     */
    public void performConnected(Transaction transaction) throws IOException {
        if (!isConnected()) {
            if (!connect()) {
                throw new IOException("2: Unable to connect to device: " + getDevice());
            }
        }
        getQueue().add(transaction);
    }

    /**
     * Performs the actions of the given transaction as soon as possible,
     * that is, before any other queued transactions, but after the actions
     * of the currently executing transaction.
     * @param builder
     */
    public void performImmediately(TransactionBuilder builder) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to device: " + getDevice());
        }
        getQueue().insert(builder.getTransaction());
    }

    public BtLEQueue getQueue() {
        return mQueue;
    }

    /**
     * Subclasses should call this method to add services they support.
     * Only supported services will be queried for characteristics.
     *
     * @param aSupportedService
     * @see #getCharacteristic(UUID)
     */
    protected void addSupportedService(UUID aSupportedService) {
        mSupportedServices.add(aSupportedService);
    }

    protected void addSupportedProfile(AbstractBleProfile<?> profile) {
        mSupportedProfiles.add(profile);
    }

    /**
     * Subclasses should call this method to add server services they support.
     * @param service
     */
    protected void addSupportedServerService(BluetoothGattService service) {
        mSupportedServerServices.add(service);
    }

    /**
     * Returns the characteristic matching the given UUID. Only characteristics
     * are returned whose service is marked as supported.
     *
     * @param uuid
     * @return the characteristic for the given UUID or <code>null</code>
     * @see #addSupportedService(UUID)
     */
    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        synchronized (characteristicsMonitor) {
            if (mAvailableCharacteristics == null) {
                return null;
            }
            return mAvailableCharacteristics.get(uuid);
        }
    }

    private void gattServicesDiscovered(List<BluetoothGattService> discoveredGattServices) {
        if (discoveredGattServices == null) {
            logger.warn("No gatt services discovered: null!");
            return;
        }
        Set<UUID> supportedServices = getSupportedServices();
        Map<UUID, BluetoothGattCharacteristic> newCharacteristics = new HashMap<>();
        for (BluetoothGattService service : discoveredGattServices) {
            if (supportedServices.contains(service.getUuid())) {
                logger.debug("discovered supported service: " + BleNamesResolver.resolveServiceName(service.getUuid().toString()) + ": " + service.getUuid());
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                if (characteristics == null || characteristics.isEmpty()) {
                    logger.warn("Supported LE service " + service.getUuid() + "did not return any characteristics");
                    continue;
                }
                HashMap<UUID, BluetoothGattCharacteristic> intmAvailableCharacteristics = new HashMap<>(characteristics.size());
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    intmAvailableCharacteristics.put(characteristic.getUuid(), characteristic);
                    logger.info("    characteristic: " + BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString()) + ": " + characteristic.getUuid());
                }
                newCharacteristics.putAll(intmAvailableCharacteristics);

                synchronized (characteristicsMonitor) {
                    mAvailableCharacteristics = newCharacteristics;
                }
            } else {
                logger.debug("discovered unsupported service: " + BleNamesResolver.resolveServiceName(service.getUuid().toString()) + ": " + service.getUuid());
            }
        }
    }

    protected Set<UUID> getSupportedServices() {
        return mSupportedServices;
    }

    /**
     * Utility method that may be used to log incoming messages when we don't know how to deal with them yet.
     *
     * @param value
     */
    public void logMessageContent(byte[] value) {
        logger.info("RECEIVED DATA WITH LENGTH: " + ((value != null) ? value.length : "(null)"));
        Logging.logBytes(logger, value);
    }

    /// Write a string of data, and chunk it up
    protected void uartTx(TransactionBuilder builder, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
        logger.info("UART TX: " + str);
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

    protected void addReceiveHistory(String s) {
        receiveHistory += s;
        if (receiveHistory.length() > MAX_RECEIVE_HISTORY_CHARS)
            receiveHistory = receiveHistory.substring(receiveHistory.length() - MAX_RECEIVE_HISTORY_CHARS);
    }

    /// Converts an object to a JSON string. see jsonToString
    private String jsonToStringInternal(Object v) {
        if (v instanceof String) {
            /* Convert a string, escaping chars we can't send over out UART connection */
            String s = (String)v;
            String json = "\"";
            //String rawString = "";
            for (int i=0;i<s.length();i++) {
                int ch = (int)s.charAt(i); // 0..255
                int nextCh = (int)(i+1<s.length() ? s.charAt(i+1) : 0); // 0..255
                //rawString = rawString+ch+",";
                if (ch<8) {
                    // if the next character is a digit, it'd be interpreted
                    // as a 2 digit octal character, so we can't use `\0` to escape it
                    if (nextCh>='0' && nextCh<='7') json += "\\x0" + ch;
                    else json += "\\" + ch;
                } else if (ch==8) json += "\\b";
                else if (ch==9) json += "\\t";
                else if (ch==10) json += "\\n";
                else if (ch==11) json += "\\v";
                else if (ch==12) json += "\\f";
                else if (ch==34) json += "\\\""; // quote
                else if (ch==92) json += "\\\\"; // slash
                else if (ch<32 || ch==127 || ch==173)
                    json += "\\x"+Integer.toHexString((ch&255)|256).substring(1);
                else json += s.charAt(i);
            }
            // if it was less characters to send base64, do that!
            if (json.length() > 5+(s.length()*4/3)) {
                byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
                return "atob(\""+ Base64.encodeToString(bytes, Base64.DEFAULT).replaceAll("\n","")+"\")";
            }
            // for debugging...
            //addReceiveHistory("\n---------------------\n"+rawString+"\n---------------------\n");
            return json + "\"";
        } else if (v instanceof JSONArray) {
            JSONArray a = (JSONArray)v;
            String json = "[";
            for (int i=0;i<a.length();i++) {
                if (i>0) json += ",";
                Object o = null;
                try {
                    o = a.get(i);
                } catch (JSONException e) {
                    logger.warn("jsonToString array error: " + e.getLocalizedMessage());
                }
                json += jsonToStringInternal(o);
            }
            return json+"]";
        } else if (v instanceof JSONObject) {
            JSONObject obj = (JSONObject)v;
            String json = "{";
            Iterator<String> iter = obj.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                Object o = null;
                try {
                    o = obj.get(key);
                } catch (JSONException e) {
                    logger.warn("jsonToString object error: " + e.getLocalizedMessage());
                }
                json += key+":"+jsonToStringInternal(o);
                if (iter.hasNext()) json+=",";
            }
            return json+"}";
        } // else int/double/null
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
    protected void uartTxJSON(String taskName, JSONObject json) {
        try {
            TransactionBuilder builder = performInitialized(taskName);
            uartTx(builder, "\u0010GB("+jsonToString(json)+")\n");
            builder.queue(getQueue());
        } catch (IOException e) {
            GB.toast(getContext(), "Error in "+taskName+": " + e.getLocalizedMessage(), Toast.LENGTH_LONG, GB.ERROR);
        }
    }

    protected void uartTxJSONError(String taskName, String message, String id) {
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


    protected void handleIntentJSON(JSONObject json) throws JSONException {
        Prefs devicePrefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress()));
        if (devicePrefs.getBoolean(PREF_DEVICE_INTENTS, false)) {
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
            logger.info("Executing intent:\n\t" + String.valueOf(in) + "\n\tTargeting: " + target);
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
                    logger.info("Targeting '"+target+"' isn't implemented or doesn't exist.");
                    GB.toast(getContext(), "Targeting '"+target+"' isn't implemented or it doesn't exist.", Toast.LENGTH_LONG, GB.INFO);
            }
        } else {
            uartTxJSONError("intent", "Android Intents not enabled, check Gadgetbridge Device Settings", null);
        }
    }

    private Intent addIntentFlag(Intent intent, String flag) {
        try {
            final Class<Intent> intentClass = Intent.class;
            final Field flagField = intentClass.getDeclaredField(flag);
            intent.addFlags(flagField.getInt(null));
        } catch (final Exception e) {
            // The user sent an invalid flag
            logger.info("Flag '"+flag+"' isn't implemented or doesn't exist and was therefore not set.");
            GB.toast(getContext(), "Flag '"+flag+"' isn't implemented or it doesn't exist and was therefore not set.", Toast.LENGTH_LONG, GB.INFO);
        }
        return intent;
    }


    // default implementations of event handler methods (gatt callbacks)
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            profile.onConnectionStateChange(gatt, status, newState);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        gattServicesDiscovered(gatt.getServices());

        if (getDevice().getState().compareTo(GBDevice.State.INITIALIZING) >= 0) {
            logger.warn("Services discovered, but device state is already " + getDevice().getState() + " for device: " + getDevice() + ", so ignoring");
            return;
        }
        initializeDevice(createTransactionBuilder("Initializing device")).queue(getQueue());
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic, int status) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            if (profile.onCharacteristicRead(gatt, characteristic, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCharacteristicWrite(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            if (profile.onCharacteristicWrite(gatt, characteristic, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            if (profile.onDescriptorRead(gatt, descriptor, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            if (profile.onDescriptorWrite(gatt, descriptor, status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            if (profile.onCharacteristicChanged(gatt, characteristic)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        for (AbstractBleProfile profile : mSupportedProfiles) {
            profile.onReadRemoteRssi(gatt, rssi, status);
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {

    }

    @Override
    public boolean onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        return false;
    }

    @Override
    public boolean onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        return false;
    }

    @Override
    public boolean onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        return false;
    }

    @Override
    public boolean onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        return false;
    }
}
