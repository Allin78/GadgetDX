/*  Copyright (C) 2015-2021 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Martin, Matthieu Baerts, Normano64, odavo32nof, Pauli Salmenrinne,
    Pavel Elagin, Petr Vaněk, Saul Nunez, Taavi Eomäe

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
package nodomain.freeyourgadget.gadgetbridge;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.util.TypedValue;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.DBOpenHelper;
import nodomain.freeyourgadget.gadgetbridge.database.PeriodicExporter;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoMaster;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.externalevents.BluetoothStateChangeReceiver;
import nodomain.freeyourgadget.gadgetbridge.externalevents.TimeChangeReceiver;
import nodomain.freeyourgadget.gadgetbridge.externalevents.opentracks.OpenTracksContentObserver;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceService;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityUser;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.model.Weather;
import nodomain.freeyourgadget.gadgetbridge.service.NotificationCollectorMonitorService;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.LimitedQueue;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.AMAZFITBIP;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.AMAZFITCOR;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.AMAZFITCOR2;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.FITPRO;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.GALAXY_BUDS;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.LEFUN;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.MIBAND;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.MIBAND2;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.MIBAND2_HRX;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.MIBAND3;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.PEBBLE;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.TLW64;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.WATCHXPLUS;
import static nodomain.freeyourgadget.gadgetbridge.model.DeviceType.fromKey;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.NOTIFICATION_CHANNEL_HIGH_PRIORITY_ID;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.NOTIFICATION_ID_ERROR;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.apache.commons.lang3.StringUtils;

/**
 * Main Application class that initializes and provides access to certain things like
 * logging and DB access.
 */
public class GBApplication extends Application {
    // Since this class must not log to slf4j, we use plain android.util.Log
    private static final String TAG = "GBApplication";
    public static final String DATABASE_NAME = "Gadgetbridge";

    private static GBApplication context;
    private static final Lock dbLock = new ReentrantLock();
    private static DeviceService deviceService;
    private static SharedPreferences sharedPrefs;
    private static final String PREFS_VERSION = "shared_preferences_version";
    //if preferences have to be migrated, increment the following and add the migration logic in migratePrefs below; see http://stackoverflow.com/questions/16397848/how-can-i-migrate-android-preferences-with-a-new-version
    private static final int CURRENT_PREFS_VERSION = 22;

    private static LimitedQueue mIDSenderLookup = new LimitedQueue(16);
    private static Prefs prefs;
    private static GBPrefs gbPrefs;
    private static LockHandler lockHandler;
    /**
     * Note: is null on Lollipop
     */
    private static NotificationManager notificationManager;

    public static final String ACTION_QUIT
            = "nodomain.freeyourgadget.gadgetbridge.gbapplication.action.quit";
    public static final String ACTION_LANGUAGE_CHANGE = "nodomain.freeyourgadget.gadgetbridge.gbapplication.action.language_change";
    public static final String ACTION_THEME_CHANGE = "nodomain.freeyourgadget.gadgetbridge.gbapplication.action.theme_change";
    public static final String ACTION_NEW_DATA = "nodomain.freeyourgadget.gadgetbridge.action.new_data";

    private static GBApplication app;

    private static Logging logging = new Logging() {
        @Override
        protected String createLogDirectory() throws IOException {
            if (GBEnvironment.env().isLocalTest()) {
                return System.getProperty(Logging.PROP_LOGFILES_DIR);
            } else {
                File dir = FileUtils.getExternalFilesDir();
                return dir.getAbsolutePath();
            }
        }
    };
    private static Locale language;

    private DeviceManager deviceManager;
    private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;

    private OpenTracksContentObserver openTracksObserver;

    private long lastAutoExportTimestamp = 0;
    private long autoExportScheduledTimestamp = 0;

    public static void quit() {
        GB.log("Quitting Gadgetbridge...", GB.INFO, null);
        Intent quitIntent = new Intent(GBApplication.ACTION_QUIT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(quitIntent);
        GBApplication.deviceService().quit();
        System.exit(0);
    }

    public GBApplication() {
        context = this;
        // don't do anything here, add it to onCreate instead

        //if (BuildConfig.DEBUG) {
        //    // detect everything
        //    //StrictMode.enableDefaults();
        //    // detect closeable objects
        //    //StrictMode.setVmPolicy(
        //    //        new StrictMode.VmPolicy.Builder()
        //    //                .detectLeakedClosableObjects()
        //    //                .penaltyLog()
        //    //                .build()
        //    //);
        //}
    }

    public static Logging getLogging() {
        return logging;
    }

    protected DeviceService createDeviceService() {
        return new GBDeviceService(this);
    }

    @Override
    public void onCreate() {
        app = this;
        super.onCreate();

        if (lockHandler != null) {
            // guard against multiple invocations (robolectric)
            return;
        }

        // Initialize the timezones library
        AndroidThreeTen.init(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs = new Prefs(sharedPrefs);
        gbPrefs = new GBPrefs(prefs);

        if (!GBEnvironment.isEnvironmentSetup()) {
            GBEnvironment.setupEnvironment(GBEnvironment.createDeviceEnvironment());
            // setup db after the environment is set up, but don't do it in test mode
            // in test mode, it's done individually, see TestBase
            setupDatabase();
        }

        // don't do anything here before we set up logging, otherwise
        // slf4j may be implicitly initialized before we properly configured it.
        setupLogging(isFileLoggingEnabled());

        if (getPrefsFileVersion() != CURRENT_PREFS_VERSION) {
            migratePrefs(getPrefsFileVersion());
        }

        setupExceptionHandler();

        Weather.getInstance().setCacheFile(getCacheDir(), prefs.getBoolean("cache_weather", true));

        deviceManager = new DeviceManager(this);
        String language = prefs.getString("language", "default");
        setLanguage(language);

        deviceService = createDeviceService();
        loadAppsNotifBlackList();
        loadAppsPebbleBlackList();

        PeriodicExporter.enablePeriodicExport(context);
        TimeChangeReceiver.scheduleNextDstChange(context);

        if (isRunningMarshmallowOrLater()) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (isRunningOreoOrLater()) {
                bluetoothStateChangeReceiver = new BluetoothStateChangeReceiver();
                registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            }
            try {
                //the following will ensure the notification manager is kept alive
                startService(new Intent(this, NotificationCollectorMonitorService.class));
            } catch (IllegalStateException e) {
                String message = e.toString();
                if (message == null) {
                    message = getString(R.string._unknown_);
                }
                GB.notify(NOTIFICATION_ID_ERROR,
                        new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_HIGH_PRIORITY_ID)
                                .setSmallIcon(R.drawable.gadgetbridge_img)
                                .setContentTitle(getString(R.string.error_background_service))
                                .setContentText(getString(R.string.error_background_service_reason_truncated))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(getString(R.string.error_background_service_reason) + "\"" + message + "\""))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .build(), context);
            }
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_BACKGROUND) {
            if (!hasBusyDevice()) {
                DBHelper.clearSession();
            }
        }
    }

    /**
     * Returns true if at least a single device is busy, e.g synchronizing activity data
     * or something similar.
     * Note: busy is not the same as connected or initialized!
     */
    private boolean hasBusyDevice() {
        List<GBDevice> devices = getDeviceManager().getDevices();
        for (GBDevice device : devices) {
            if (device.isBusy()) {
                return true;
            }
        }
        return false;
    }

    public static void setupLogging(boolean enabled) {
        logging.setupLogging(enabled);
    }

    public static String getLogPath() {
        return logging.getLogPath();
    }

    private void setupExceptionHandler() {
        LoggingExceptionHandler handler = new LoggingExceptionHandler(Thread.getDefaultUncaughtExceptionHandler());
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    public static boolean isFileLoggingEnabled() {
        return prefs.getBoolean("log_to_file", false);
    }

    public static boolean minimizeNotification() {
        return prefs.getBoolean("minimize_priority", false);
    }

    public void setupDatabase() {
        DaoMaster.OpenHelper helper;
        GBEnvironment env = GBEnvironment.env();
        if (env.isTest()) {
            helper = new DaoMaster.DevOpenHelper(this, null, null);
        } else {
            helper = new DBOpenHelper(this, DATABASE_NAME, null);
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        if (lockHandler == null) {
            lockHandler = new LockHandler();
        }
        lockHandler.init(daoMaster, helper);
    }

    public static Context getContext() {
        return context;
    }

    /**
     * Returns the facade for talking to devices. Devices are managed by
     * an Android Service and this facade provides access to its functionality.
     *
     * @return the facade for talking to the service/devices.
     */
    public static DeviceService deviceService() {
        return deviceService;
    }

    /**
     * Returns the facade for talking to a specific device. Devices are managed by
     * an Android Service and this facade provides access to its functionality.
     *
     * @return the facade for talking to the service/device.
     */
    public static DeviceService deviceService(GBDevice device) {
        return deviceService.forDevice(device);
    }

    /**
     * Returns the DBHandler instance for reading/writing or throws GBException
     * when that was not successful
     * If acquiring was successful, callers must call #releaseDB when they
     * are done (from the same thread that acquired the lock!
     * <p>
     * Callers must not hold a reference to the returned instance because it
     * will be invalidated at some point.
     *
     * @return the DBHandler
     * @throws GBException
     * @see #releaseDB()
     */
    public static DBHandler acquireDB() throws GBException {
        try {
            if (dbLock.tryLock(30, TimeUnit.SECONDS)) {
                return lockHandler;
            }
        } catch (InterruptedException ex) {
            Log.i(TAG, "Interrupted while waiting for DB lock");
        }
        throw new GBException("Unable to access the database.");
    }

    /**
     * Releases the database lock.
     *
     * @throws IllegalMonitorStateException if the current thread is not owning the lock
     * @see #acquireDB()
     */
    public static void releaseDB() {
        dbLock.unlock();
    }

    public static boolean isRunningMarshmallowOrLater() {
        return VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isRunningNougatOrLater() {
        return VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isRunningOreoOrLater() {
        return VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isRunningTenOrLater() {
        return VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isRunningTwelveOrLater() {
        return VERSION.SDK_INT >= 31;  // Build.VERSION_CODES.S, but our target SDK is lower
    }

    public static boolean isRunningPieOrLater() {
        return VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    private static boolean isPrioritySender(int prioritySenders, String number) {
        if (prioritySenders == Policy.PRIORITY_SENDERS_ANY) {
            return true;
        } else {
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] projection = new String[]{PhoneLookup._ID, PhoneLookup.STARRED};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            boolean exists = false;
            int starred = 0;
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    exists = true;
                    starred = cursor.getInt(cursor.getColumnIndexOrThrow(PhoneLookup.STARRED));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (prioritySenders == Policy.PRIORITY_SENDERS_CONTACTS && exists) {
                return true;
            } else if (prioritySenders == Policy.PRIORITY_SENDERS_STARRED && starred == 1) {
                return true;
            }
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isPriorityNumber(int priorityType, String number) {
        NotificationManager.Policy notificationPolicy = notificationManager.getNotificationPolicy();
        if (priorityType == Policy.PRIORITY_CATEGORY_MESSAGES) {
            if ((notificationPolicy.priorityCategories & Policy.PRIORITY_CATEGORY_MESSAGES) == Policy.PRIORITY_CATEGORY_MESSAGES) {
                return isPrioritySender(notificationPolicy.priorityMessageSenders, number);
            }
        } else if (priorityType == Policy.PRIORITY_CATEGORY_CALLS) {
            if ((notificationPolicy.priorityCategories & Policy.PRIORITY_CATEGORY_CALLS) == Policy.PRIORITY_CATEGORY_CALLS) {
                return isPrioritySender(notificationPolicy.priorityCallSenders, number);
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static int getGrantedInterruptionFilter() {
        if (GBApplication.isRunningMarshmallowOrLater() && notificationManager.isNotificationPolicyAccessGranted()) {
            return notificationManager.getCurrentInterruptionFilter();
        }
        return NotificationManager.INTERRUPTION_FILTER_ALL;
    }

    private static HashSet<String> apps_notification_blacklist = null;

    public static boolean appIsNotifBlacklisted(String packageName) {
        if (apps_notification_blacklist == null) {
            GB.log("appIsNotifBlacklisted: apps_notification_blacklist is null!", GB.INFO, null);
        }
        return apps_notification_blacklist != null && apps_notification_blacklist.contains(packageName);
    }

    public static void setAppsNotifBlackList(Set<String> packageNames) {
        setAppsNotifBlackList(packageNames, sharedPrefs.edit());
    }

    public static void setAppsNotifBlackList(Set<String> packageNames, SharedPreferences.Editor editor) {
        if (packageNames == null) {
            GB.log("Set null apps_notification_blacklist", GB.INFO, null);
            apps_notification_blacklist = new HashSet<>();
        } else {
            apps_notification_blacklist = new HashSet<>(packageNames);
        }
        GB.log("New apps_notification_blacklist has " + apps_notification_blacklist.size() + " entries", GB.INFO, null);
        saveAppsNotifBlackList(editor);
    }

    private static void loadAppsNotifBlackList() {
        GB.log("Loading apps_notification_blacklist", GB.INFO, null);
        apps_notification_blacklist = (HashSet<String>) sharedPrefs.getStringSet(GBPrefs.PACKAGE_BLACKLIST, null); // lgtm [java/abstract-to-concrete-cast]
        if (apps_notification_blacklist == null) {
            apps_notification_blacklist = new HashSet<>();
        }
        GB.log("Loaded apps_notification_blacklist has " + apps_notification_blacklist.size() + " entries", GB.INFO, null);
    }

    private static void saveAppsNotifBlackList() {
       saveAppsNotifBlackList(sharedPrefs.edit());
    }

    private static void saveAppsNotifBlackList(SharedPreferences.Editor editor) {
        GB.log("Saving apps_notification_blacklist with " + apps_notification_blacklist.size() + " entries", GB.INFO, null);
        if (apps_notification_blacklist.isEmpty()) {
            editor.putStringSet(GBPrefs.PACKAGE_BLACKLIST, null);
        } else {
            Prefs.putStringSet(editor, GBPrefs.PACKAGE_BLACKLIST, apps_notification_blacklist);
        }
        editor.apply();
    }

    public static void addAppToNotifBlacklist(String packageName) {
        if (apps_notification_blacklist.add(packageName)) {
            saveAppsNotifBlackList();
        }
    }

    public static synchronized void removeFromAppsNotifBlacklist(String packageName) {
        GB.log("Removing from apps_notification_blacklist: " + packageName, GB.INFO, null);
        apps_notification_blacklist.remove(packageName);
        saveAppsNotifBlackList();
    }

    private static HashSet<String> apps_pebblemsg_blacklist = null;

    public static boolean appIsPebbleBlacklisted(String sender) {
        if (apps_pebblemsg_blacklist == null) {
            GB.log("appIsPebbleBlacklisted: apps_pebblemsg_blacklist is null!", GB.INFO, null);
        }
        return apps_pebblemsg_blacklist != null && apps_pebblemsg_blacklist.contains(sender);
    }

    public static void setAppsPebbleBlackList(Set<String> packageNames) {
        setAppsPebbleBlackList(packageNames, sharedPrefs.edit());
    }

    public static void setAppsPebbleBlackList(Set<String> packageNames, SharedPreferences.Editor editor) {
        if (packageNames == null) {
            GB.log("Set null apps_pebblemsg_blacklist", GB.INFO, null);
            apps_pebblemsg_blacklist = new HashSet<>();
        } else {
            apps_pebblemsg_blacklist = new HashSet<>(packageNames);
        }
        GB.log("New apps_pebblemsg_blacklist has " + apps_pebblemsg_blacklist.size() + " entries", GB.INFO, null);
        saveAppsPebbleBlackList(editor);
    }

    private static void loadAppsPebbleBlackList() {
        GB.log("Loading apps_pebblemsg_blacklist", GB.INFO, null);
        apps_pebblemsg_blacklist = (HashSet<String>) sharedPrefs.getStringSet(GBPrefs.PACKAGE_PEBBLEMSG_BLACKLIST, null); // lgtm [java/abstract-to-concrete-cast]
        if (apps_pebblemsg_blacklist == null) {
            apps_pebblemsg_blacklist = new HashSet<>();
        }
        GB.log("Loaded apps_pebblemsg_blacklist has " + apps_pebblemsg_blacklist.size() + " entries", GB.INFO, null);
    }

    private static void saveAppsPebbleBlackList() {
       saveAppsPebbleBlackList(sharedPrefs.edit());
    }

    private static void saveAppsPebbleBlackList(SharedPreferences.Editor editor) {
        GB.log("Saving apps_pebblemsg_blacklist with " + apps_pebblemsg_blacklist.size() + " entries", GB.INFO, null);
        if (apps_pebblemsg_blacklist.isEmpty()) {
            editor.putStringSet(GBPrefs.PACKAGE_PEBBLEMSG_BLACKLIST, null);
        } else {
            Prefs.putStringSet(editor, GBPrefs.PACKAGE_PEBBLEMSG_BLACKLIST, apps_pebblemsg_blacklist);
        }
        editor.apply();
    }

    public static void addAppToPebbleBlacklist(String packageName) {
        if (apps_pebblemsg_blacklist.add(packageNameToPebbleMsgSender(packageName))) {
            saveAppsPebbleBlackList();
        }
    }

    public static synchronized void removeFromAppsPebbleBlacklist(String packageName) {
        GB.log("Removing from apps_pebblemsg_blacklist: " + packageName, GB.INFO, null);
        apps_pebblemsg_blacklist.remove(packageNameToPebbleMsgSender(packageName));
        saveAppsPebbleBlackList();
    }

    public static String packageNameToPebbleMsgSender(String packageName) {
        if ("eu.siacs.conversations".equals(packageName)) {
            return ("Conversations");
        } else if ("net.osmand.plus".equals(packageName)) {
            return ("OsmAnd");
        }
        return packageName;
    }

    /**
     * Deletes both the old Activity database and the new one recreates it with empty tables.
     *
     * @return true on successful deletion
     */
    public static synchronized boolean deleteActivityDatabase(Context context) {
        // TODO: flush, close, reopen db
        if (lockHandler != null) {
            lockHandler.closeDb();
        }
        boolean result = deleteOldActivityDatabase(context);
        result &= getContext().deleteDatabase(DATABASE_NAME);
        return result;
    }

    /**
     * Deletes the legacy (pre 0.12) Activity database
     *
     * @return true on successful deletion
     */
    public static synchronized boolean deleteOldActivityDatabase(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        boolean result = true;
        if (dbHelper.existsDB("ActivityDatabase")) {
            result = getContext().deleteDatabase("ActivityDatabase");
        }
        return result;
    }

    private int getPrefsFileVersion() {
        try {
            return Integer.parseInt(sharedPrefs.getString(PREFS_VERSION, "0")); //0 is legacy
        } catch (Exception e) {
            //in version 1 this was an int
            return 1;
        }
    }

    private void migrateStringPrefToPerDevicePref(String globalPref, String globalPrefDefault, String perDevicePref, ArrayList<DeviceType> deviceTypes) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String globalPrefValue = prefs.getString(globalPref, globalPrefDefault);
        try (DBHandler db = acquireDB()) {
            DaoSession daoSession = db.getDaoSession();
            List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
            for (Device dbDevice : activeDevices) {
                SharedPreferences deviceSpecificSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                if (deviceSpecificSharedPrefs != null) {
                    SharedPreferences.Editor deviceSharedPrefsEdit = deviceSpecificSharedPrefs.edit();
                    DeviceType deviceType = fromKey(dbDevice.getType());

                    if (deviceTypes.contains(deviceType)) {
                        Log.i(TAG, "migrating global string preference " + globalPref + " for " + deviceType.name() + " " + dbDevice.getIdentifier() );
                        deviceSharedPrefsEdit.putString(perDevicePref, globalPrefValue);
                    }
                    deviceSharedPrefsEdit.apply();
                }
            }
            editor.remove(globalPref);
            editor.apply();
        } catch (Exception e) {
            Log.w(TAG, "error acquiring DB lock");
        }
    }

    private void migrateBooleanPrefToPerDevicePref(String globalPref, Boolean globalPrefDefault, String perDevicePref, ArrayList<DeviceType> deviceTypes) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        boolean globalPrefValue = prefs.getBoolean(globalPref, globalPrefDefault);
        try (DBHandler db = acquireDB()) {
            DaoSession daoSession = db.getDaoSession();
            List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
            for (Device dbDevice : activeDevices) {
                SharedPreferences deviceSpecificSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                if (deviceSpecificSharedPrefs != null) {
                    SharedPreferences.Editor deviceSharedPrefsEdit = deviceSpecificSharedPrefs.edit();
                    DeviceType deviceType = fromKey(dbDevice.getType());

                    if (deviceTypes.contains(deviceType)) {
                        Log.i(TAG, "migrating global boolean preference " + globalPref + " for " + deviceType.name() + " " + dbDevice.getIdentifier() );
                        deviceSharedPrefsEdit.putBoolean(perDevicePref, globalPrefValue);
                    }
                    deviceSharedPrefsEdit.apply();
                }
            }
            editor.remove(globalPref);
            editor.apply();
        } catch (Exception e) {
            Log.w(TAG, "error acquiring DB lock");
        }
    }

    private void migratePrefs(int oldVersion) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (oldVersion == 0) {
            String legacyGender = sharedPrefs.getString("mi_user_gender", null);
            String legacyHeight = sharedPrefs.getString("mi_user_height_cm", null);
            String legacyWeight = sharedPrefs.getString("mi_user_weight_kg", null);
            String legacyYOB = sharedPrefs.getString("mi_user_year_of_birth", null);
            if (legacyGender != null) {
                int gender = "male".equals(legacyGender) ? 1 : "female".equals(legacyGender) ? 0 : 2;
                editor.putString(ActivityUser.PREF_USER_GENDER, Integer.toString(gender));
                editor.remove("mi_user_gender");
            }
            if (legacyHeight != null) {
                editor.putString(ActivityUser.PREF_USER_HEIGHT_CM, legacyHeight);
                editor.remove("mi_user_height_cm");
            }
            if (legacyWeight != null) {
                editor.putString(ActivityUser.PREF_USER_WEIGHT_KG, legacyWeight);
                editor.remove("mi_user_weight_kg");
            }
            if (legacyYOB != null) {
                editor.putString(ActivityUser.PREF_USER_YEAR_OF_BIRTH, legacyYOB);
                editor.remove("mi_user_year_of_birth");
            }
        }
        if (oldVersion < 2) {
            //migrate the integer version of gender introduced in version 1 to a string value, needed for the way Android accesses the shared preferences
            int legacyGender_1 = 2;
            try {
                legacyGender_1 = sharedPrefs.getInt(ActivityUser.PREF_USER_GENDER, 2);
            } catch (Exception e) {
                Log.e(TAG, "Could not access legacy activity gender", e);
            }
            editor.putString(ActivityUser.PREF_USER_GENDER, Integer.toString(legacyGender_1));
        }
        if (oldVersion < 3) {
            try (DBHandler db = acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
                for (Device dbDevice : activeDevices) {
                    SharedPreferences deviceSpecificSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    if (deviceSpecificSharedPrefs != null) {
                        SharedPreferences.Editor deviceSharedPrefsEdit = deviceSpecificSharedPrefs.edit();
                        String preferenceKey = dbDevice.getIdentifier() + "_lastSportsActivityTimeMillis";
                        long lastSportsActivityTimeMillis = sharedPrefs.getLong(preferenceKey, 0);
                        if (lastSportsActivityTimeMillis != 0) {
                            deviceSharedPrefsEdit.putLong("lastSportsActivityTimeMillis", lastSportsActivityTimeMillis);
                            editor.remove(preferenceKey);
                        }
                        preferenceKey = dbDevice.getIdentifier() + "_lastSyncTimeMillis";
                        long lastSyncTimeMillis = sharedPrefs.getLong(preferenceKey, 0);
                        if (lastSyncTimeMillis != 0) {
                            deviceSharedPrefsEdit.putLong("lastSyncTimeMillis", lastSyncTimeMillis);
                            editor.remove(preferenceKey);
                        }

                        String newLanguage = null;
                        Set<String> displayItems = null;

                        DeviceType deviceType = fromKey(dbDevice.getType());

                        if (deviceType == AMAZFITBIP || deviceType == AMAZFITCOR || deviceType == AMAZFITCOR2) {
                            int oldLanguage = prefs.getInt("amazfitbip_language", -1);
                            newLanguage = "auto";
                            String[] oldLanguageLookup = {"zh_CN", "zh_TW", "en_US", "es_ES", "ru_RU", "de_DE", "it_IT", "fr_FR", "tr_TR"};
                            if (oldLanguage >= 0 && oldLanguage < oldLanguageLookup.length) {
                                newLanguage = oldLanguageLookup[oldLanguage];
                            }
                        }

                        if (deviceType == AMAZFITBIP || deviceType == AMAZFITCOR) {
                            deviceSharedPrefsEdit.putString("disconnect_notification", prefs.getString("disconnect_notification", "off"));
                            deviceSharedPrefsEdit.putString("disconnect_notification_start", prefs.getString("disconnect_notification_start", "8:00"));
                            deviceSharedPrefsEdit.putString("disconnect_notification_end", prefs.getString("disconnect_notification_end", "22:00"));
                        }
                        if (deviceType == MIBAND2 || deviceType == MIBAND2_HRX || deviceType == MIBAND3) {
                            deviceSharedPrefsEdit.putString("do_not_disturb", prefs.getString("mi2_do_not_disturb", "off"));
                            deviceSharedPrefsEdit.putString("do_not_disturb_start", prefs.getString("mi2_do_not_disturb_start", "1:00"));
                            deviceSharedPrefsEdit.putString("do_not_disturb_end", prefs.getString("mi2_do_not_disturb_end", "6:00"));
                        }
                        if (dbDevice.getManufacturer().equals("Huami")) {
                            deviceSharedPrefsEdit.putString("activate_display_on_lift_wrist", prefs.getString("activate_display_on_lift_wrist", "off"));
                            deviceSharedPrefsEdit.putString("display_on_lift_start", prefs.getString("display_on_lift_start", "0:00"));
                            deviceSharedPrefsEdit.putString("display_on_lift_end", prefs.getString("display_on_lift_end", "0:00"));
                        }
                        switch (deviceType) {
                            case MIBAND:
                                deviceSharedPrefsEdit.putBoolean("low_latency_fw_update", prefs.getBoolean("mi_low_latency_fw_update", true));
                                deviceSharedPrefsEdit.putString("device_time_offset_hours", String.valueOf(prefs.getInt("mi_device_time_offset_hours", 0)));
                                break;
                            case AMAZFITCOR:
                                displayItems = prefs.getStringSet("cor_display_items", null);
                                break;
                            case AMAZFITBIP:
                                displayItems = prefs.getStringSet("bip_display_items", null);
                                break;
                            case MIBAND2:
                            case MIBAND2_HRX:
                                displayItems = prefs.getStringSet("mi2_display_items", null);
                                deviceSharedPrefsEdit.putBoolean("mi2_enable_text_notifications", prefs.getBoolean("mi2_enable_text_notifications", true));
                                deviceSharedPrefsEdit.putString("mi2_dateformat", prefs.getString("mi2_dateformat", "dateformat_time"));
                                deviceSharedPrefsEdit.putBoolean("rotate_wrist_to_cycle_info", prefs.getBoolean("mi2_rotate_wrist_to_switch_info", false));
                                break;
                            case MIBAND3:
                                newLanguage = prefs.getString("miband3_language", "auto");
                                displayItems = prefs.getStringSet("miband3_display_items", null);
                                deviceSharedPrefsEdit.putBoolean("swipe_unlock", prefs.getBoolean("mi3_band_screen_unlock", false));
                                deviceSharedPrefsEdit.putString("night_mode", prefs.getString("mi3_night_mode", "off"));
                                deviceSharedPrefsEdit.putString("night_mode_start", prefs.getString("mi3_night_mode_start", "16:00"));
                                deviceSharedPrefsEdit.putString("night_mode_end", prefs.getString("mi3_night_mode_end", "7:00"));

                        }
                        if (displayItems != null) {
                            deviceSharedPrefsEdit.putStringSet("display_items", displayItems);
                        }
                        if (newLanguage != null) {
                            deviceSharedPrefsEdit.putString("language", newLanguage);
                        }
                        deviceSharedPrefsEdit.apply();
                    }
                }
                editor.remove("amazfitbip_language");
                editor.remove("bip_display_items");
                editor.remove("cor_display_items");
                editor.remove("disconnect_notification");
                editor.remove("disconnect_notification_start");
                editor.remove("disconnect_notification_end");
                editor.remove("activate_display_on_lift_wrist");
                editor.remove("display_on_lift_start");
                editor.remove("display_on_lift_end");

                editor.remove("mi_low_latency_fw_update");
                editor.remove("mi_device_time_offset_hours");
                editor.remove("mi2_do_not_disturb");
                editor.remove("mi2_do_not_disturb_start");
                editor.remove("mi2_do_not_disturb_end");
                editor.remove("mi2_dateformat");
                editor.remove("mi2_display_items");
                editor.remove("mi2_rotate_wrist_to_switch_info");
                editor.remove("mi2_enable_text_notifications");
                editor.remove("mi3_band_screen_unlock");
                editor.remove("mi3_night_mode");
                editor.remove("mi3_night_mode_start");
                editor.remove("mi3_night_mode_end");
                editor.remove("miband3_language");

            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }
        if (oldVersion < 4) {
            try (DBHandler db = acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
                for (Device dbDevice : activeDevices) {
                    SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();
                    DeviceType deviceType = fromKey(dbDevice.getType());

                    if (deviceType == MIBAND) {
                        int deviceTimeOffsetHours = deviceSharedPrefs.getInt("device_time_offset_hours",0);
                        deviceSharedPrefsEdit.putString("device_time_offset_hours", Integer.toString(deviceTimeOffsetHours) );
                    }

                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }
        if (oldVersion < 5) {
            try (DBHandler db = acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
                for (Device dbDevice : activeDevices) {
                    SharedPreferences deviceSpecificSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    if (deviceSpecificSharedPrefs != null) {
                        SharedPreferences.Editor deviceSharedPrefsEdit = deviceSpecificSharedPrefs.edit();
                        DeviceType deviceType = fromKey(dbDevice.getType());

                        String newWearside = null;
                        String newOrientation = null;
                        String newTimeformat = null;
                        switch (deviceType) {
                            case AMAZFITBIP:
                            case AMAZFITCOR:
                            case AMAZFITCOR2:
                            case MIBAND:
                            case MIBAND2:
                            case MIBAND2_HRX:
                            case MIBAND3:
                            case MIBAND4:
                                newWearside = prefs.getString("mi_wearside", "left");
                                break;
                            case MIBAND5:
                                newWearside = prefs.getString("mi_wearside", "left");
                                break;
                            case HPLUS:
                                newWearside = prefs.getString("hplus_wrist", "left");
                                newTimeformat = prefs.getString("hplus_timeformat", "24h");
                                break;
                            case ID115:
                                newWearside = prefs.getString("id115_wrist", "left");
                                newOrientation = prefs.getString("id115_screen_orientation", "horizontal");
                                break;
                            case ZETIME:
                                newWearside = prefs.getString("zetime_wrist", "left");
                                newTimeformat = prefs.getInt("zetime_timeformat", 1) == 2 ? "am/pm" : "24h";
                                break;
                        }
                        if (newWearside != null) {
                            deviceSharedPrefsEdit.putString("wearlocation", newWearside);
                        }
                        if (newOrientation != null) {
                            deviceSharedPrefsEdit.putString("screen_orientation", newOrientation);
                        }
                        if (newTimeformat != null) {
                            deviceSharedPrefsEdit.putString("timeformat", newTimeformat);
                        }
                        deviceSharedPrefsEdit.apply();
                    }
                }
                editor.remove("hplus_timeformat");
                editor.remove("hplus_wrist");
                editor.remove("id115_wrist");
                editor.remove("id115_screen_orientation");
                editor.remove("mi_wearside");
                editor.remove("zetime_timeformat");
                editor.remove("zetime_wrist");

            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }
        if (oldVersion < 6) {
            migrateBooleanPrefToPerDevicePref("mi2_enable_button_action", false, "button_action_enable", new ArrayList<>(Collections.singletonList(MIBAND2)));
            migrateBooleanPrefToPerDevicePref("mi2_button_action_vibrate", false, "button_action_vibrate", new ArrayList<>(Collections.singletonList(MIBAND2)));
            migrateStringPrefToPerDevicePref("mi_button_press_count", "6", "button_action_press_count", new ArrayList<>(Collections.singletonList(MIBAND2)));
            migrateStringPrefToPerDevicePref("mi_button_press_count_max_delay", "2000", "button_action_press_max_interval", new ArrayList<>(Collections.singletonList(MIBAND2)));
            migrateStringPrefToPerDevicePref("mi_button_press_count_match_delay", "0", "button_action_broadcast_delay", new ArrayList<>(Collections.singletonList(MIBAND2)));
            migrateStringPrefToPerDevicePref("mi_button_press_broadcast", "nodomain.freeyourgadget.gadgetbridge.ButtonPressed", "button_action_broadcast", new ArrayList<>(Collections.singletonList(MIBAND2)));
        }
        if (oldVersion < 7) {
            migrateStringPrefToPerDevicePref("mi_reserve_alarm_calendar", "0", "reserve_alarms_calendar", new ArrayList<>(Arrays.asList(MIBAND, MIBAND2)));
        }

        if (oldVersion < 8) {
            for (int i = 1; i <= 16; i++) {
                String message = prefs.getString("canned_message_dismisscall_" + i, null);
                if (message != null) {
                    migrateStringPrefToPerDevicePref("canned_message_dismisscall_" + i, "", "canned_message_dismisscall_" + i, new ArrayList<>(Collections.singletonList(PEBBLE)));
                }
            }
            for (int i = 1; i <= 16; i++) {
                String message = prefs.getString("canned_reply_" + i, null);
                if (message != null) {
                    migrateStringPrefToPerDevicePref("canned_reply_" + i, "", "canned_reply_" + i, new ArrayList<>(Collections.singletonList(PEBBLE)));
                }
            }
        }
        if (oldVersion < 9) {
            try (DBHandler db = acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
                migrateBooleanPrefToPerDevicePref("transliteration", false, "pref_transliteration_enabled", (ArrayList)activeDevices);
                Log.w(TAG, "migrating transliteration settings");
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock and migrating prefs");
            }
        }
        if (oldVersion < 10) {
            //migrate the string version of pref_galaxy_buds_ambient_volume to int due to transition to SeekBarPreference
            try (DBHandler db = acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
                for (Device dbDevice : activeDevices) {
                    SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();
                    DeviceType deviceType = fromKey(dbDevice.getType());

                    if (deviceType == GALAXY_BUDS) {
                        GB.log("migrating Galaxy Buds volume", GB.INFO, null);
                        String volume = deviceSharedPrefs.getString(DeviceSettingsPreferenceConst.PREF_GALAXY_BUDS_AMBIENT_VOLUME, "1");
                        deviceSharedPrefsEdit.putInt(DeviceSettingsPreferenceConst.PREF_GALAXY_BUDS_AMBIENT_VOLUME, Integer.parseInt(volume));
                    }
                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }
        if (oldVersion < 11) {
            try (DBHandler db = acquireDB()) {
                DaoSession daoSession = db.getDaoSession();
                List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);
                for (Device dbDevice : activeDevices) {
                    SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();
                    DeviceType deviceType = fromKey(dbDevice.getType());
                    if (deviceType == WATCHXPLUS || deviceType == FITPRO || deviceType == LEFUN) {
                        deviceSharedPrefsEdit.putBoolean("inactivity_warnings_enable", deviceSharedPrefs.getBoolean("pref_longsit_switch", false));
                        deviceSharedPrefsEdit.remove("pref_longsit_switch");
                    }
                    if (deviceType == WATCHXPLUS || deviceType == FITPRO) {
                        deviceSharedPrefsEdit.putString("inactivity_warnings_start", deviceSharedPrefs.getString("pref_longsit_start", "06:00"));
                        deviceSharedPrefsEdit.putString("inactivity_warnings_end", deviceSharedPrefs.getString("pref_longsit_end", "23:00"));
                        deviceSharedPrefsEdit.remove("pref_longsit_start");
                        deviceSharedPrefsEdit.remove("pref_longsit_end");
                    }
                    if (deviceType == WATCHXPLUS || deviceType == LEFUN) {
                        deviceSharedPrefsEdit.putString("inactivity_warnings_threshold", deviceSharedPrefs.getString("pref_longsit_period", "60"));
                        deviceSharedPrefsEdit.remove("pref_longsit_period");
                    }
                    if (deviceType == TLW64) {
                        deviceSharedPrefsEdit.putBoolean("inactivity_warnings_enable_noshed", deviceSharedPrefs.getBoolean("screen_longsit_noshed", false));
                        deviceSharedPrefsEdit.remove("screen_longsit_noshed");
                    }
                    if (dbDevice.getManufacturer().equals("Huami")) {
                        editor.putBoolean("inactivity_warnings_dnd", prefs.getBoolean("mi2_inactivity_warnings_dnd", false));
                        editor.putString("inactivity_warnings_dnd_start", prefs.getString("mi2_inactivity_warnings_dnd_start", "12:00"));
                        editor.putString("inactivity_warnings_dnd_end", prefs.getString("mi2_inactivity_warnings_dnd_end", "14:00"));
                        editor.putBoolean("inactivity_warnings_enable", prefs.getBoolean("mi2_inactivity_warnings", false));
                        editor.putInt("inactivity_warnings_threshold", prefs.getInt("mi2_inactivity_warnings_threshold", 60));
                        editor.putString("inactivity_warnings_start", prefs.getString("mi2_inactivity_warnings_start", "06:00"));
                        editor.putString("inactivity_warnings_end", prefs.getString("mi2_inactivity_warnings_end", "22:00"));
                    }
                    switch (deviceType) {
                        case LEFUN:
                            deviceSharedPrefsEdit.putString("language", deviceSharedPrefs.getString("pref_lefun_interface_language", "0"));
                            deviceSharedPrefsEdit.remove("pref_lefun_interface_language");
                            break;
                        case FITPRO:
                            deviceSharedPrefsEdit.putString("inactivity_warnings_threshold", deviceSharedPrefs.getString("pref_longsit_period", "4"));
                            deviceSharedPrefsEdit.remove("pref_longsit_period");
                            break;
                        case ZETIME:
                            editor.putString("do_not_disturb", prefs.getString("zetime_do_not_disturb", "off"));
                            editor.putString("do_not_disturb_start", prefs.getString("zetime_do_not_disturb_start", "22:00"));
                            editor.putString("do_not_disturb_end", prefs.getString("zetime_do_not_disturb_end", "07:00"));
                            editor.putBoolean("inactivity_warnings_enable", prefs.getBoolean("zetime_inactivity_warnings", false));
                            editor.putString("inactivity_warnings_start", prefs.getString("zetime_inactivity_warnings_start", "06:00"));
                            editor.putString("inactivity_warnings_end", prefs.getString("zetime_inactivity_warnings_end", "22:00"));
                            editor.putInt("inactivity_warnings_threshold", prefs.getInt("zetime_inactivity_warnings_threshold", 60));
                            editor.putBoolean("inactivity_warnings_mo", prefs.getBoolean("zetime_prefs_inactivity_repetitions_mo", false));
                            editor.putBoolean("inactivity_warnings_tu", prefs.getBoolean("zetime_prefs_inactivity_repetitions_tu", false));
                            editor.putBoolean("inactivity_warnings_we", prefs.getBoolean("zetime_prefs_inactivity_repetitions_we", false));
                            editor.putBoolean("inactivity_warnings_th", prefs.getBoolean("zetime_prefs_inactivity_repetitions_th", false));
                            editor.putBoolean("inactivity_warnings_fr", prefs.getBoolean("zetime_prefs_inactivity_repetitions_fr", false));
                            editor.putBoolean("inactivity_warnings_sa", prefs.getBoolean("zetime_prefs_inactivity_repetitions_sa", false));
                            editor.putBoolean("inactivity_warnings_su", prefs.getBoolean("zetime_prefs_inactivity_repetitions_su", false));
                            break;
                    }
                    deviceSharedPrefsEdit.apply();
                }
                editor.putInt("fitness_goal", prefs.getInt("mi_fitness_goal", 8000));

                editor.remove("zetime_do_not_disturb");
                editor.remove("zetime_do_not_disturb_start");
                editor.remove("zetime_do_not_disturb_end");
                editor.remove("zetime_inactivity_warnings");
                editor.remove("zetime_inactivity_warnings_start");
                editor.remove("zetime_inactivity_warnings_end");
                editor.remove("zetime_inactivity_warnings_threshold");
                editor.remove("zetime_prefs_inactivity_repetitions_mo");
                editor.remove("zetime_prefs_inactivity_repetitions_tu");
                editor.remove("zetime_prefs_inactivity_repetitions_we");
                editor.remove("zetime_prefs_inactivity_repetitions_th");
                editor.remove("zetime_prefs_inactivity_repetitions_fr");
                editor.remove("zetime_prefs_inactivity_repetitions_sa");
                editor.remove("zetime_prefs_inactivity_repetitions_su");
                editor.remove("mi2_inactivity_warnings_dnd");
                editor.remove("mi2_inactivity_warnings_dnd_start");
                editor.remove("mi2_inactivity_warnings_dnd_end");
                editor.remove("mi2_inactivity_warnings");
                editor.remove("mi2_inactivity_warnings_threshold");
                editor.remove("mi2_inactivity_warnings_start");
                editor.remove("mi2_inactivity_warnings_end");
                editor.remove("mi_fitness_goal");
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }
        if (oldVersion < 12) {
            // Convert preferences that were wrongly migrated to int, since Android saves them as Strings internally
            editor.putString("inactivity_warnings_threshold", String.valueOf(prefs.getInt("inactivity_warnings_threshold", 60)));
            editor.putString("fitness_goal", String.valueOf(prefs.getInt("fitness_goal", 8000)));
        }

        if (oldVersion < 13) {
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();

                    if (dbDevice.getManufacturer().equals("Huami")) {
                        deviceSharedPrefsEdit.putBoolean("inactivity_warnings_enable", prefs.getBoolean("inactivity_warnings_enable", false));
                        deviceSharedPrefsEdit.putString("inactivity_warnings_threshold", prefs.getString("inactivity_warnings_threshold", "60"));
                        deviceSharedPrefsEdit.putString("inactivity_warnings_start", prefs.getString("inactivity_warnings_start", "06:00"));
                        deviceSharedPrefsEdit.putString("inactivity_warnings_end", prefs.getString("inactivity_warnings_end", "22:00"));

                        deviceSharedPrefsEdit.putBoolean("inactivity_warnings_dnd", prefs.getBoolean("inactivity_warnings_dnd", false));
                        deviceSharedPrefsEdit.putString("inactivity_warnings_dnd_start", prefs.getString("inactivity_warnings_dnd_start", "12:00"));
                        deviceSharedPrefsEdit.putString("inactivity_warnings_dnd_end", prefs.getString("inactivity_warnings_dnd_end", "14:00"));

                        deviceSharedPrefsEdit.putBoolean("fitness_goal_notification", prefs.getBoolean("mi2_goal_notification", false));
                    }

                    // Not removing the first 4 preferences since they're still used by some devices (ZeTime)
                    editor.remove("inactivity_warnings_dnd");
                    editor.remove("inactivity_warnings_dnd_start");
                    editor.remove("inactivity_warnings_dnd_end");
                    editor.remove("mi2_goal_notification");

                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 14) {
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();

                    if (DeviceType.MIBAND.equals(dbDevice.getType()) || dbDevice.getManufacturer().equals("Huami")) {
                        deviceSharedPrefsEdit.putBoolean("heartrate_sleep_detection", prefs.getBoolean("mi_hr_sleep_detection", false));
                        deviceSharedPrefsEdit.putString("heartrate_measurement_interval", prefs.getString("heartrate_measurement_interval", "0"));
                    }

                    // Not removing heartrate_measurement_interval since it's still used by some devices (ZeTime)
                    editor.remove("mi_hr_sleep_detection");

                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 15) {
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();

                    if (DeviceType.FITPRO.equals(dbDevice.getType())) {
                        editor.remove("inactivity_warnings_threshold");
                        deviceSharedPrefsEdit.apply();
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 16) {
            // If transliteration was enabled for a device, migrate it to the per-language setting
            final String defaultLanguagesIfEnabled = "extended_ascii,common_symbols,scandinavian,german,russian,hebrew,greek,ukranian,arabic,persian,latvian,lithuanian,polish,estonian,icelandic,czech,turkish,bengali,korean";
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();

                    if (deviceSharedPrefs.getBoolean("pref_transliteration_enabled", false)) {
                        deviceSharedPrefsEdit.putString("pref_transliteration_languages", defaultLanguagesIfEnabled);
                    }

                    deviceSharedPrefsEdit.remove("pref_transliteration_enabled");

                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 17) {
            final HashSet<String> calendarBlacklist = (HashSet<String>) prefs.getStringSet(GBPrefs.CALENDAR_BLACKLIST, null);

            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();

                    deviceSharedPrefsEdit.putBoolean("sync_calendar", prefs.getBoolean("enable_calendar_sync", true));

                    if (calendarBlacklist != null) {
                        Prefs.putStringSet(deviceSharedPrefsEdit, GBPrefs.CALENDAR_BLACKLIST, calendarBlacklist);
                    }

                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }

            editor.remove(GBPrefs.CALENDAR_BLACKLIST);
        }

        if (oldVersion < 18) {
            // Migrate the default value for Huami find band vibration pattern
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    if (!dbDevice.getManufacturer().equals("Huami")) {
                        continue;
                    }

                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();

                    deviceSharedPrefsEdit.putString("huami_vibration_profile_find_band", "long");
                    deviceSharedPrefsEdit.putString("huami_vibration_count_find_band", "1");

                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 19) {
            //remove old ble scanning prefences, now unsupported
            editor.remove("disable_new_ble_scanning");
        }

        if (oldVersion < 20) {
            // Add the new stress tab to all devices
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (final Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());

                    final String chartsTabsValue = deviceSharedPrefs.getString("charts_tabs", null);
                    if (chartsTabsValue == null) {
                        continue;
                    }

                    final String newPrefValue;
                    if (!StringUtils.isBlank(chartsTabsValue)) {
                        newPrefValue = chartsTabsValue + ",stress";
                    } else {
                        newPrefValue = "stress";
                    }

                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();
                    deviceSharedPrefsEdit.putString("charts_tabs", newPrefValue);
                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 21) {
            // Add the new PAI tab to all devices
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (final Device dbDevice : activeDevices) {
                    final SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());

                    final String chartsTabsValue = deviceSharedPrefs.getString("charts_tabs", null);
                    if (chartsTabsValue == null) {
                        continue;
                    }

                    final String newPrefValue;
                    if (!StringUtils.isBlank(chartsTabsValue)) {
                        newPrefValue = chartsTabsValue + ",pai";
                    } else {
                        newPrefValue = "pai";
                    }

                    final SharedPreferences.Editor deviceSharedPrefsEdit = deviceSharedPrefs.edit();
                    deviceSharedPrefsEdit.putString("charts_tabs", newPrefValue);
                    deviceSharedPrefsEdit.apply();
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        if (oldVersion < 22) {
            try (DBHandler db = acquireDB()) {
                final DaoSession daoSession = db.getDaoSession();
                final List<Device> activeDevices = DBHelper.getActiveDevices(daoSession);

                for (Device dbDevice : activeDevices) {
                    final DeviceType deviceType = fromKey(dbDevice.getType());
                    if (deviceType == MIBAND2) {
                        final String name = dbDevice.getName();
                        if ("Mi Band HRX".equalsIgnoreCase(name) || "Mi Band 2i".equalsIgnoreCase(name)) {
                            dbDevice.setType(DeviceType.MIBAND2_HRX.getKey());
                            daoSession.getDeviceDao().update(dbDevice);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "error acquiring DB lock");
            }
        }

        editor.putString(PREFS_VERSION, Integer.toString(CURRENT_PREFS_VERSION));
        editor.apply();
    }

    public static SharedPreferences getDeviceSpecificSharedPrefs(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isEmpty()) {
            return null;
        }
        return context.getSharedPreferences("devicesettings_" + deviceIdentifier, Context.MODE_PRIVATE);
    }

    public static void deleteDeviceSpecificSharedPrefs(String deviceIdentifier) {
        if (deviceIdentifier == null || deviceIdentifier.isEmpty()) {
            return;
        }
        context.getSharedPreferences("devicesettings_" + deviceIdentifier, Context.MODE_PRIVATE).edit().clear().apply();
    }


    public static void setLanguage(String lang) {
        if (lang.equals("default")) {
            language = Resources.getSystem().getConfiguration().locale;
        } else {
            language = new Locale(lang);
        }
        updateLanguage(language);
    }

    public static void updateLanguage(Locale locale) {
        AndroidUtils.setLanguage(context, locale);

        Intent intent = new Intent();
        intent.setAction(ACTION_LANGUAGE_CHANGE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static LimitedQueue getIDSenderLookup() {
        return mIDSenderLookup;
    }

    public static boolean isDarkThemeEnabled() {
        String selectedTheme = prefs.getString("pref_key_theme", context.getString(R.string.pref_theme_value_system));
        Resources resources = context.getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                (selectedTheme.equals(context.getString(R.string.pref_theme_value_system)) || selectedTheme.equals(context.getString(R.string.pref_theme_value_dynamic)))) {
            return (resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        } else {
            return selectedTheme.equals(context.getString(R.string.pref_theme_value_dark));
        }
    }

    public static boolean isAmoledBlackEnabled() {
        return prefs.getBoolean("pref_key_theme_amoled_black", false);
    }

    public static boolean areDynamicColorsEnabled() {
        String selectedTheme = prefs.getString("pref_key_theme", context.getString(R.string.pref_theme_value_system));
        return selectedTheme.equals(context.getString(R.string.pref_theme_value_dynamic));
    }

    public static int getTextColor(Context context) {
        if (GBApplication.isDarkThemeEnabled()) {
            return context.getResources().getColor(R.color.primarytext_dark);
        } else {
            return context.getResources().getColor(R.color.primarytext_light);
        }
    }

    public static int getSecondaryTextColor(Context context) {
        return context.getResources().getColor(R.color.secondarytext);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLanguage(getLanguage());
    }

    public static int getBackgroundColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.background, typedValue, true);
        return typedValue.data;
    }

    public static int getWindowBackgroundColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true);
        return typedValue.data;
    }

    public static Prefs getPrefs() {
        return prefs;
    }

    public static GBPrefs getGBPrefs() {
        return gbPrefs;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public static GBApplication app() {
        return app;
    }

    public static Locale getLanguage() {
        return language;
    }

    public static boolean isNightly() {
        return BuildConfig.APPLICATION_ID.contains("nightly");
    }

    public String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            GB.log("Unable to determine Gadgetbridge's version", GB.WARN, e);
            return "0.0.0";
        }
    }

    public String getNameAndVersion() {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
            return String.format("%s %s", appInfo.name, packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            GB.log("Unable to determine Gadgetbridge's name/version", GB.WARN, e);
            return "Gadgetbridge";
        }
    }

    public void setOpenTracksObserver(OpenTracksContentObserver openTracksObserver) {
        this.openTracksObserver = openTracksObserver;
    }

    public OpenTracksContentObserver getOpenTracksObserver() {
        return openTracksObserver;
    }

    public long getLastAutoExportTimestamp() {
        return lastAutoExportTimestamp;
    }

    public void setLastAutoExportTimestamp(long lastAutoExportTimestamp) {
        this.lastAutoExportTimestamp = lastAutoExportTimestamp;
    }

    public long getAutoExportScheduledTimestamp() {
        return autoExportScheduledTimestamp;
    }

    public void setAutoExportScheduledTimestamp(long autoExportScheduledTimestamp) {
        this.autoExportScheduledTimestamp = autoExportScheduledTimestamp;
    }
}
