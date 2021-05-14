package nodomain.freeyourgadget.gadgetbridge.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.preference.Preference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;

import static nodomain.freeyourgadget.gadgetbridge.model.SpeechToText.PREF_STT_SCORER;
import static nodomain.freeyourgadget.gadgetbridge.model.SpeechToText.PREF_STT_TFLITE;

public class SpeechToTextPreferencesActivity extends AbstractSettingsActivity {
    private static final Logger LOG = LoggerFactory.getLogger(GBPrefs.class);

    private List prefs = Arrays.asList(PREF_STT_TFLITE, PREF_STT_SCORER);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.stt_preferences);
        for(int i = 0;i < prefs.size();i++) {
            Preference filePicker = findPreference(prefs.get(i).toString());
            int finalI = i;
            filePicker.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");
                startActivityForResult(intent, finalI);
                return true;
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        for(int i = 0;i < prefs.size();i++) {
            if (data != null && requestCode == i) {
                String filename = null;
                try {
                    InputStream inputStream = getContentResolver().openInputStream(Uri.parse(data.getDataString()));
                    File dir = FileUtils.getExternalFilesDir();
                    byte[] b = new byte[4096];
                    File targetFile = new File(dir, prefs.get(i).toString());
                    filename = targetFile.getAbsolutePath();
                    FileChannel wChannel = new FileOutputStream(targetFile, true).getChannel();
                    while (inputStream.read(b) >= 0) {
                        wChannel.write(ByteBuffer.wrap(b));
                    }
                    wChannel.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                editor.putString(prefs.get(i).toString(), filename);
                editor.apply();
                LOG.debug(filename);
            }
        }
    }

    @Override
    protected String[] getPreferenceKeysWithSummary() {
        return new String[]{
                PREF_STT_TFLITE,
                PREF_STT_SCORER,};
    }
}