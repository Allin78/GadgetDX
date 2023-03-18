/*  Copyright (C) 2016-2020 Alberto, Andreas Shimokawa, Carsten Pfeiffer,
    Daniele Gobbetti, vanous

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
package nodomain.freeyourgadget.gadgetbridge.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_management);

        TextView dbPath = findViewById(R.id.activity_data_management_path);
        dbPath.setText(FileUtils.getExportFilesDirString());

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

        Button showContentDataButton = findViewById(R.id.showContentDataButton);
        showContentDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File export_path = null;
                export_path = FileUtils.getExportFilesDir();

                AlertDialog.Builder builder = new AlertDialog.Builder(DataManagementActivity.this);
                builder.setTitle("Export/Import directory content:");

                ArrayAdapter<String> directory_listing = new ArrayAdapter<String>(DataManagementActivity.this, android.R.layout.simple_list_item_1, export_path.list());

                builder.setSingleChoiceItems(directory_listing, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        Button exportImportSettingsButton = findViewById(R.id.exportImportSettingsButton);
        exportImportSettingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataManagementActivity.this, DataManagementPreferencesActivity.class);
                startActivity(intent);
            }
        });

        int oldDBVisibility = hasOldActivityDatabase() ? View.VISIBLE : View.GONE;

        TextView deleteOldActivityTitle = findViewById(R.id.mergeOldActivityDataTitle);
        deleteOldActivityTitle.setVisibility(oldDBVisibility);

        Button deleteOldActivityDBButton = findViewById(R.id.deleteOldActivityDB);
        deleteOldActivityDBButton.setVisibility(oldDBVisibility);
        deleteOldActivityDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOldActivityDbFile();
            }
        });

        Button deleteDBButton = findViewById(R.id.emptyDBButton);
        deleteDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteActivityDatabase();
            }
        });

        TextView dbPath2 = findViewById(R.id.activity_data_management_path2);
        dbPath2.setText(FileUtils.getExportFilesDirString());

        Button cleanExportDirectoryButton = findViewById(R.id.cleanExportDirectoryButton);
        cleanExportDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanExportDirectory();
            }
        });
        GBApplication gbApp = GBApplication.app();
        Prefs prefs = GBApplication.getPrefs();
        boolean autoExportEnabled = prefs.getBoolean(GBPrefs.AUTO_EXPORT_ENABLED, false);
        int autoExportInterval = prefs.getInt(GBPrefs.AUTO_EXPORT_INTERVAL, 0);

        int testExportVisibility = (autoExportInterval > 0 && autoExportEnabled) ? View.VISIBLE : View.GONE;
        boolean isExportEnabled = autoExportInterval > 0 && autoExportEnabled;
        TextView autoExportLocation_label = findViewById(R.id.autoExportLocation_label);
        autoExportLocation_label.setVisibility(testExportVisibility);

        TextView autoExportLocation_path = findViewById(R.id.autoExportLocation_path);
        autoExportLocation_path.setVisibility(testExportVisibility);
        autoExportLocation_path.setText(FileUtils.getExportFilesDirString());

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

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView dbPath = findViewById(R.id.activity_data_management_path);
        dbPath.setText(FileUtils.getExportFilesDirString());

        int oldDBVisibility = hasOldActivityDatabase() ? View.VISIBLE : View.GONE;
        TextView deleteOldActivityTitle = findViewById(R.id.mergeOldActivityDataTitle);
        deleteOldActivityTitle.setVisibility(oldDBVisibility);

        TextView dbPath2 = findViewById(R.id.activity_data_management_path2);
        dbPath2.setText(FileUtils.getExportFilesDirString());


        GBApplication gbApp = GBApplication.app();
        Prefs prefs = GBApplication.getPrefs();
        boolean autoExportEnabled = prefs.getBoolean(GBPrefs.AUTO_EXPORT_ENABLED, false);
        int autoExportInterval = prefs.getInt(GBPrefs.AUTO_EXPORT_INTERVAL, 0);

        int testExportVisibility = (autoExportInterval > 0 && autoExportEnabled) ? View.VISIBLE : View.GONE;
        boolean isExportEnabled = autoExportInterval > 0 && autoExportEnabled;
        TextView autoExportLocation_label = findViewById(R.id.autoExportLocation_label);
        autoExportLocation_label.setVisibility(testExportVisibility);

        TextView autoExportLocation_path = findViewById(R.id.autoExportLocation_path);
        autoExportLocation_path.setVisibility(testExportVisibility);
        autoExportLocation_path.setText(FileUtils.getExportFilesDirString());

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
    }

    private boolean hasOldActivityDatabase() {
        return new DBHelper(this).existsDB("ActivityDatabase");
    }

    private void importShared() {
        try {
            File myPath = FileUtils.getExportFilesDir();
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
                    File myPath = FileUtils.getExportFilesDir();
                    File myFile = new File(myPath, "Export_preference_" + FileUtils.makeValidFileName(dbDevice.getIdentifier()));

                    if (!myFile.exists()) { //first try to use file in new format de_ad_be_af, if doesn't exist use old format de:at:be:af
                        myFile = new File(myPath, "Export_preference_" + dbDevice.getIdentifier());
                        LOG.info("Trying to import with older filename");
                    }else{
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
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.dbmanagementactivity_export_data_title)
                .setMessage(R.string.dbmanagementactivity_export_confirmation)
                .setPositiveButton(R.string.activity_DB_ExportButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            FileUtils.performFullExport(DataManagementActivity.this);
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_exported_to, FileUtils.getExportFilesDirString()), Toast.LENGTH_LONG, GB.INFO);
                        } catch(Exception ex)
                        {
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
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.dbmanagementactivity_import_data_title)
                .setMessage(R.string.dbmanagementactivity_overwrite_database_confirmation)
                .setPositiveButton(R.string.dbmanagementactivity_overwrite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try (DBHandler dbHandler = GBApplication.acquireDB()) {
                            DBHelper helper = new DBHelper(DataManagementActivity.this);
                            File dir = FileUtils.getExportFilesDir();
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

    private void deleteActivityDatabase() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.dbmanagementactivity_delete_activity_data_title)
                .setMessage(R.string.dbmanagementactivity_really_delete_entire_db)
                .setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (GBApplication.deleteActivityDatabase(DataManagementActivity.this)) {
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_database_successfully_deleted), Toast.LENGTH_SHORT, GB.INFO);
                        } else {
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_db_deletion_failed), Toast.LENGTH_SHORT, GB.INFO);
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

    private void deleteOldActivityDbFile() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.dbmanagementactivity_delete_old_activity_db)
                .setIcon(R.drawable.ic_warning)
                .setMessage(R.string.dbmanagementactivity_delete_old_activitydb_confirmation)
                .setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (GBApplication.deleteOldActivityDatabase(DataManagementActivity.this)) {
                    GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_old_activity_db_successfully_deleted), Toast.LENGTH_SHORT, GB.INFO);
                } else {
                    GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_old_activity_db_deletion_failed), Toast.LENGTH_SHORT, GB.INFO);
                }
            }
        });
        new AlertDialog.Builder(this).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        new AlertDialog.Builder(this).show();
    }

    private void cleanExportDirectory() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.activity_DB_clean_export_directory_warning_title)
                .setMessage(getString(R.string.activity_DB_clean_export_directory_warning_message))
                .setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            File externalFilesDir = FileUtils.getExportFilesDir();
                            for (File file : externalFilesDir.listFiles()) {
                                if (file.isFile() &&
                                        (!FileUtils.getExtension(file.toString()).toLowerCase().equals("gpx"))  //keep GPX files
                                ) {
                                    LOG.debug("Deleting file: " + file);
                                    try {
                                        file.delete();
                                    } catch (Exception exception) {
                                        LOG.error("Error erasing file: " + exception);
                                    }
                                }
                            }
                            GB.toast(getString(R.string.dbmanagementactivity_export_finished), Toast.LENGTH_SHORT, GB.INFO);
                        } catch (Exception ex) {
                            GB.toast(DataManagementActivity.this, getString(R.string.dbmanagementactivity_error_cleaning_export_directory, ex.getMessage()), Toast.LENGTH_LONG, GB.ERROR, ex);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
