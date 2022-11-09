package nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.qhybrid.QHybridSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;

public class WithingsCalibrationActivity extends AbstractGBActivity {

    private GBDevice device;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withings_calibration);

        device = getIntent().getParcelableExtra(GBDevice.EXTRA_DEVICE);
        if (device == null) {
            Toast.makeText(this, R.string.watch_not_connected, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initView();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(new Intent(WithingsSteelHRDeviceSupport.START_HANDS_CALIBRATION_CMD));
    }

    @Override
    protected void onDestroy() {
        localBroadcastManager.sendBroadcast(new Intent(WithingsSteelHRDeviceSupport.STOP_HANDS_CALIBRATION_CMD));
        super.onDestroy();
    }

    private void initView() {
        RotaryControl rotaryControl = findViewById(R.id.rotary_control);
        rotaryControl.setRotationListener(new RotaryControl.RotationListener() {
            @Override
            public void onRotation(double pos) {
                Intent calibration = new Intent(WithingsSteelHRDeviceSupport.HANDS_CALIBRATION_CMD);
                calibration.putExtra("position", pos);
                localBroadcastManager.sendBroadcast(calibration);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}