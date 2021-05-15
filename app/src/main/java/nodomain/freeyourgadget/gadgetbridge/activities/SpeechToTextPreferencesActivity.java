package nodomain.freeyourgadget.gadgetbridge.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.preference.Preference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;

import static nodomain.freeyourgadget.gadgetbridge.GBApplication.app;
import static nodomain.freeyourgadget.gadgetbridge.model.SpeechToText.PREF_STT_SCORER;
import static nodomain.freeyourgadget.gadgetbridge.model.SpeechToText.PREF_STT_TFLITE;

public class SpeechToTextPreferencesActivity extends AbstractSettingsActivity {

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
            if (requestCode == i) {
                try {
                    String filename;
                    File targetFile = new File(FileUtils.getExternalFilesDir(), prefs.get(i).toString());
                    if (targetFile.exists())
                        targetFile.delete();
                    if (data != null) {
                        InputStream inputStream = getContentResolver().openInputStream(Uri.parse(data.getDataString()));
                        byte[] b = new byte[4096];

                        filename = targetFile.getAbsolutePath();
                        FileChannel wChannel = new FileOutputStream(targetFile, true).getChannel();
                        while (inputStream.read(b) >= 0) {
                            wChannel.write(ByteBuffer.wrap(b));
                        }
                        wChannel.close();
                    } else
                        filename = null;
                    editor.putString(prefs.get(i).toString(), filename);
                    editor.apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Restart model
        app().derefModel();
        app().getModel();
    }
}