/*  Copyright (C) 2021-2024 Arjan Schrijver, Petr Vaněk

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
package nodomain.freeyourgadget.gadgetbridge.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NavUtils;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.files.FileManagerActivity;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.database.PeriodicExporter;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.DateTimeUtils;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.ImportExportSharedPreferences;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;


public class DataManagementActivity extends AbstractGBActivity {
    private static final Logger LOG = LoggerFactory.getLogger(DataManagementActivity.class);
    private static SharedPreferences sharedPrefs;
    ActivityResultLauncher<Intent> selectResult;
    Uri rootUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_management);

        TextView dbPath = findViewById(R.id.activity_data_management_path);
        dbPath.setText(getExternalPath());

        Button exportDBButton = findViewById(R.id.exportDataButton);
        exportDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDB();
            }
        });
        Button importDBButton = findViewById(R.id.importDataButton);
        importDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importDB();
            }
        });

        Button selectContentDataButton = findViewById(R.id.selectContentDataButton);
        selectContentDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectExternalDir();
            }
        });

        selectResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if (data != null) {
                                Uri uri = data.getData();
                                if (uri != null) {
                                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    GBApplication.getPrefs().getPreferences()
                                            .edit()
                                            .putString(GBPrefs.EXPORT_LOCATION, uri.toString())
                                            .apply();
                                    TextView dbPath = findViewById(R.id.activity_data_management_path);
                                    dbPath.setText(uri.toString());
                                }

                            }
                        }
                    }
                });

        Button showContentDataButton = findViewById(R.id.showContentDataButton);
        showContentDataButton.setOnClickListener(v -> {
            final Intent fileManagerIntent = new Intent(DataManagementActivity.this, FileManagerActivity.class);
            startActivity(fileManagerIntent);
        });

        GBApplication gbApp = GBApplication.app();
        Prefs prefs = GBApplication.getPrefs();
        boolean autoExportEnabled = prefs.getBoolean(GBPrefs.AUTO_EXPORT_ENABLED, false);
        int autoExportInterval = prefs.getInt(GBPrefs.AUTO_EXPORT_INTERVAL, 0);
        //returns an ugly content://...
        //String autoExportLocation = prefs.getString(GBPrefs.AUTO_EXPORT_LOCATION, "");

        int testExportVisibility = (autoExportInterval > 0 && autoExportEnabled) ? View.VISIBLE : View.GONE;
        boolean isExportEnabled = autoExportInterval > 0 && autoExportEnabled;
        TextView autoExportLocation_label = findViewById(R.id.autoExportLocation_label);
        autoExportLocation_label.setVisibility(testExportVisibility);

        TextView autoExportLocation_path = findViewById(R.id.autoExportLocation_path);
        autoExportLocation_path.setVisibility(testExportVisibility);
        autoExportLocation_path.setText(getAutoExportLocationUserString() + " (" + getAutoExportLocationPreferenceString() + ")");

        TextView autoExportEnabled_label = findViewById(R.id.autoExportEnabled);
        if (isExportEnabled) {
            autoExportEnabled_label.setText(getString(R.string.activity_db_management_autoexport_enabled_yes));
        } else {
            autoExportEnabled_label.setText(getString(R.string.activity_db_management_autoexport_enabled_no));
        }

        TextView autoExportScheduled = findViewById(R.id.autoExportScheduled);
        autoExportScheduled.setVisibility(testExportVisibility);
        long setAutoExportScheduledTimestamp = gbApp.getAutoExportScheduledTimestamp();
        if (setAutoExportScheduledTimestamp > 0) {
            autoExportScheduled.setText(getString(R.string.activity_db_management_autoexport_scheduled_yes,
                    DateTimeUtils.formatDateTime(new Date(setAutoExportScheduledTimestamp))));
        } else {
            autoExportScheduled.setText(getResources().getString(R.string.activity_db_management_autoexport_scheduled_no));
        }

        TextView autoExport_lastTime_label = findViewById(R.id.autoExport_lastTime_label);
        long lastAutoExportTimestamp = gbApp.getLastAutoExportTimestamp();

        autoExport_lastTime_label.setVisibility(View.GONE);
        autoExport_lastTime_label.setText(getString(R.string.autoExport_lastTime_label,
                DateTimeUtils.formatDateTime(new Date(lastAutoExportTimestamp))));

        if (lastAutoExportTimestamp > 0) {
            autoExport_lastTime_label.setVisibility(testExportVisibility);
            autoExport_lastTime_label.setVisibility(testExportVisibility);
        }

        final Context context = getApplicationContext();
        Button testExportDBButton = findViewById(R.id.testExportDBButton);
        testExportDBButton.setVisibility(testExportVisibility);
        testExportDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(context, PeriodicExporter.class));
                GB.toast(context,
                        context.getString(R.string.activity_DB_test_export_message),
                        Toast.LENGTH_SHORT, GB.INFO);
            }
        });

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void selectExternalDir() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        selectResult.launch(intent);
    }

    private String getAutoExportLocationPreferenceString() {
        String autoExportLocation = GBApplication.getPrefs().getString(GBPrefs.AUTO_EXPORT_LOCATION, null);
        if (autoExportLocation == null) {
            return "";
        }
        return autoExportLocation;
    }

    private String getAutoExportLocationUri() {
        String autoExportLocation = getAutoExportLocationPreferenceString();
        if (autoExportLocation == null) {
            return "";
        }
        Uri uri = Uri.parse(autoExportLocation);
        try {

            return AndroidUtils.getFilePath(getApplicationContext(), uri);
        } catch (IllegalArgumentException e) {
            LOG.error("getFilePath did not work, trying to resolve content provider path");
            try {
                Cursor cursor = getContentResolver().query(
                        uri,
                        new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME},
                        null, null, null, null
                );
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                }
            } catch (Exception exception) {
                LOG.error("Error getting export path", exception);
            }
        }
        return "";
    }

    private String getAutoExportLocationUserString() {
        String location = getAutoExportLocationUri();
        if (location == "") {
            return getString(R.string.activity_db_management_autoexport_location);
        }
        return location;
    }

    private String getExternalPath() {
        String exportLocation = GBApplication.getPrefs().getString(GBPrefs.EXPORT_LOCATION, null);
        if (exportLocation == null) {
            return "";
        }
        return exportLocation;
    }

    private void exportShared(DocumentFile root) {
        try {
            DocumentFile file = root.findFile("Export_preference.xml");
            if (file == null) {
                file = root.createFile("application/xml", "Export_preference");
            }
            try (OutputStream out = getContentResolver().openOutputStream(file.getUri())) {
                ImportExportSharedPreferences.exportToFile(sharedPrefs, out, null);
            }
        } catch (IOException ex) {
            GB.toast(this, getString(R.string.dbmanagementactivity_error_exporting_shared, ex.getMessage()), Toast.LENGTH_LONG, GB.ERROR, ex);
        }
        try (DBHandler lockHandler = GBApplication.acquireDB()) {
            List<Device> activeDevices = DBHelper.getActiveDevices(lockHandler.getDaoSession());
            for (Device dbDevice : activeDevices) {
                SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                if (sharedPrefs != null) {
                    String name = "Export_preference_" + FileUtils.makeValidFileName(dbDevice.getIdentifier());

                    DocumentFile file =  root.findFile(name + ".xml");
                    if (file == null) {
                        root.createFile("application/xml", name);
                    }
                    try (OutputStream out = getContentResolver().openOutputStream(file.getUri())) {
                        ImportExportSharedPreferences.exportToFile(deviceSharedPrefs, out, null);
                    } catch (Exception ignore) {
                        // some devices no not have device specific preferences
                    }
                }
            }
        } catch (Exception e) {
            GB.toast("Error exporting device specific preferences", Toast.LENGTH_SHORT, GB.ERROR, e);
        }
    }

    private void exportDBFile(DocumentFile root, DBHandler dbHandler) throws IOException {
        DBHelper helper = new DBHelper(DataManagementActivity.this);
        DocumentFile file = root.findFile(dbHandler.getHelper().getDatabaseName());
        if (file == null) {
            root.createFile("application/x-sqlite3", dbHandler.getHelper().getDatabaseName());
        }
        try (OutputStream out = getContentResolver().openOutputStream(file.getUri())) {
            helper.exportDB(dbHandler, out);
        }
    }

    private void importShared() {
        try {
            File myPath = FileUtils.getExternalFilesDir();
            File myFile = new File(myPath, "Export_preference");
            ImportExportSharedPreferences.importFromFile(sharedPrefs, myFile);
        } catch (Exception ex) {
            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_error_importing_db, ex.getMessage()), Toast.LENGTH_LONG, GB.ERROR, ex);
        }

        try (DBHandler lockHandler = GBApplication.acquireDB()) {
            List<Device> activeDevices = DBHelper.getActiveDevices(lockHandler.getDaoSession());
            for (Device dbDevice : activeDevices) {
                SharedPreferences deviceSharedPrefs = GBApplication.getDeviceSpecificSharedPrefs(dbDevice.getIdentifier());
                if (sharedPrefs != null) {
                    File myPath = FileUtils.getExternalFilesDir();
                    File myFile = new File(myPath, "Export_preference_" + FileUtils.makeValidFileName(dbDevice.getIdentifier()));

                    if (!myFile.exists()) { //first try to use file in new format de_ad_be_af, if doesn't exist use old format de:at:be:af
                        myFile = new File(myPath, "Export_preference_" + dbDevice.getIdentifier());
                        LOG.info("Trying to import with older filename");
                    } else {
                        LOG.info("Trying to import with new filename");
                    }

                    try {
                        ImportExportSharedPreferences.importFromFile(deviceSharedPrefs, myFile);
                    } catch (Exception ignore) {
                        // some devices no not have device specific preferences
                    }
                }
            }
        } catch (Exception e) {
            GB.toast("Error importing device specific preferences", Toast.LENGTH_SHORT, GB.ERROR);
        }
    }

    private void exportDB() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.dbmanagementactivity_export_data_title)
                .setMessage(R.string.dbmanagementactivity_export_confirmation)
                .setPositiveButton(R.string.activity_DB_ExportButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try (DBHandler dbHandler = GBApplication.acquireDB()) {
                            String exportLocation = getExternalPath();
                            if (exportLocation.equals("")) {
                                GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_error_exporting_db, getString(R.string.dbmanagementactivity_export_location_not_set)), Toast.LENGTH_LONG, GB.ERROR);
                                return;
                            }
                            Uri rootUri = Uri.parse(exportLocation);
                            DocumentFile root = DocumentFile.fromTreeUri(getApplicationContext(), rootUri);
                            exportShared(root);
                            exportDBFile(root, dbHandler);
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_exported_to, getExternalPath()), Toast.LENGTH_LONG, GB.INFO);
                        } catch (Exception ex) {
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_error_exporting_db, ex.getMessage()), Toast.LENGTH_LONG, GB.ERROR, ex);
                        }
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void importDB() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.dbmanagementactivity_import_data_title)
                .setMessage(R.string.dbmanagementactivity_overwrite_database_confirmation)
                .setPositiveButton(R.string.dbmanagementactivity_overwrite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try (DBHandler dbHandler = GBApplication.acquireDB()) {
                            DBHelper helper = new DBHelper(DataManagementActivity.this);
                            File dir = FileUtils.getExternalFilesDir();
                            SQLiteOpenHelper sqLiteOpenHelper = dbHandler.getHelper();
                            File sourceFile = new File(dir, sqLiteOpenHelper.getDatabaseName());
                            helper.importDB(dbHandler, sourceFile);
                            helper.validateDB(sqLiteOpenHelper);
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_import_successful), Toast.LENGTH_LONG, GB.INFO);
                        } catch (Exception ex) {
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_error_importing_db, ex.getMessage()), Toast.LENGTH_LONG, GB.ERROR, ex);
                        }
                        importShared();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
