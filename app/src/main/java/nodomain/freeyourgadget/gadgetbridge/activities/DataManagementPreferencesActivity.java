package nodomain.freeyourgadget.gadgetbridge.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.PeriodicExporter;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.SAFFileUtils;

public class DataManagementPreferencesActivity extends AbstractSettingsActivity {
    private static final int DIRECTORY_REQUEST_CODE = 4711;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_management_preferences);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Prefs prefs = GBApplication.getPrefs();

        Preference pref = findPreference(GBPrefs.EXPORT_LOCATION);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent .addFlags( Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                intent.putExtra("android.content.extra.FANCY", true);
                intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

                startActivityForResult(intent, DIRECTORY_REQUEST_CODE);
                return true;
            }
        });
        pref.setSummary(GBApplication.getPrefs().getString(GBPrefs.EXPORT_LOCATION, FileUtils.getDefaultExportFilesDirString()));

        pref = findPreference(GBPrefs.AUTO_EXPORT_INTERVAL);
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object autoExportInterval) {
                String summary = String.format(
                        getApplicationContext().getString(R.string.pref_summary_auto_export_interval),
                        Integer.valueOf((String) autoExportInterval));
                preference.setSummary(summary);
                boolean auto_export_enabled = GBApplication.getPrefs().getBoolean(GBPrefs.AUTO_EXPORT_ENABLED, false);
                PeriodicExporter.scheduleAlarm(getApplicationContext(), Integer.valueOf((String) autoExportInterval), auto_export_enabled);
                return true;
            }
        });
        int autoExportInterval = GBApplication.getPrefs().getInt(GBPrefs.AUTO_EXPORT_INTERVAL, 0);
        String summary = String.format(
                getApplicationContext().getString(R.string.pref_summary_auto_export_interval),
                (int) autoExportInterval);
        pref.setSummary(summary);

        findPreference(GBPrefs.AUTO_EXPORT_ENABLED).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object autoExportEnabled) {
                int autoExportInterval = GBApplication.getPrefs().getInt(GBPrefs.AUTO_EXPORT_INTERVAL, 0);
                PeriodicExporter.scheduleAlarm(getApplicationContext(), autoExportInterval, (boolean) autoExportEnabled);
                return true;
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == DIRECTORY_REQUEST_CODE && intent != null) {
            Uri uri = intent.getData();
            String absoluteUri = SAFFileUtils.getAbsolutePathFromSAFUri(this, uri);
            PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putString(GBPrefs.EXPORT_LOCATION, absoluteUri)
                    .apply();
            String summary =  GBApplication.getPrefs().getString(GBPrefs.EXPORT_LOCATION, FileUtils.getDefaultExportFilesDirString());
            findPreference(GBPrefs.EXPORT_LOCATION).setSummary(summary);
            boolean autoExportEnabled = GBApplication
                    .getPrefs().getBoolean(GBPrefs.AUTO_EXPORT_ENABLED, false);
            int autoExportPeriod = GBApplication
                    .getPrefs().getInt(GBPrefs.AUTO_EXPORT_INTERVAL, 0);
            PeriodicExporter.scheduleAlarm(getApplicationContext(), autoExportPeriod, autoExportEnabled);
        }
    }
}
