package nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

// TODO: implement this and add a proper layout instead of the one copied from watch9
public class WithingsCalibrationActivity extends AbstractGBActivity {

    private GBDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withings_calibration);

        Intent intent = getIntent();
        List<GBDevice> devices = GBApplication.app().getDeviceManager().getSelectedDevices();
        boolean atLeastOneConnected = false;
        for(GBDevice device : devices){
            if(device.getType() == DeviceType.WITHINGS_STEEL_HR){
                atLeastOneConnected = true;
                this.device = device;
                break;
            }
        }

        if (device == null){
            Toast.makeText(this, R.string.watch_not_connected, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        RotaryControl rotaryControl = findViewById(R.id.rotary_control);
        rotaryControl.setRotationListener(new RotaryControl.RotationListener() {
            @Override
            public void onRotation(double pos) {

            }
        });
    }
}