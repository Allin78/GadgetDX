package nodomain.freeyourgadget.gadgetbridge.devices.vesc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.service.devices.vesc.VescDeviceSupport;

public class VescControlActivity extends AbstractGBActivity {
    private static final String TAG = "VescControlActivity";
    private boolean volumeKeyPressed = false;
    private boolean volumeKeysControl = false;
    private int currentRPM = 0;
    private int currentBreakCurrentMa = 0;
    LocalBroadcastManager localBroadcastManager;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vesc_control);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setCurrent(0);
    }

    private boolean handleKeyPress(int keyCode, boolean isPressed){
        if(!volumeKeysControl){
            return false;
        }

        if(keyCode != 24 && keyCode != 25){
            return false;
        }

        if(volumeKeyPressed == isPressed){
            return true;
        }
        volumeKeyPressed = isPressed;

        logger.debug("volume " + (keyCode == 25 ? "down" : "up") + (isPressed ? " pressed" : " released"));
        if(!isPressed){
            setRPM(0);
            return true;
        }
        if(keyCode == 24){
            setRPM(currentRPM);
        }else{
            setBreakCurrent(VescControlActivity.this.currentBreakCurrentMa);
        }

        return true;
    }

    private void initViews(){
        ((CheckBox)findViewById(R.id.vesc_control_checkbox_volume_keys))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        VescControlActivity.this.volumeKeysControl = isChecked;
                        if(!isChecked){
                            setRPM(0);
                        }
                    }
                });

        ((EditText) findViewById(R.id.vesc_control_input_rpm))
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = s.toString();
                        VescControlActivity.this.currentRPM = Integer.parseInt(text);
                    }
                });

        ((EditText) findViewById(R.id.vesc_control_input_break_current))
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = s.toString();
                        VescControlActivity.this.currentBreakCurrentMa = Integer.parseInt(text) * 1000;
                    }
                });

        View.OnTouchListener controlTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(v.getId() == R.id.vesc_control_button_fwd){
                        setRPM(VescControlActivity.this.currentRPM);
                    }else{
                        setBreakCurrent(VescControlActivity.this.currentBreakCurrentMa);
                    }
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    setCurrent(0);
                }else{
                    return false;
                }
                return true;
            }
        };

        findViewById(R.id.vesc_control_button_fwd).setOnTouchListener(controlTouchListener);
        findViewById(R.id.vesc_control_button_break).setOnTouchListener(controlTouchListener);
    }

    private void setBreakCurrent(int breakCurrentMa){
        logger.debug("setting break current to {}", breakCurrentMa);
        Intent intent = new Intent(VescDeviceSupport.COMMAND_SET_BREAK_CURRENT);
        intent.putExtra(VescDeviceSupport.EXTRA_CURRENT, breakCurrentMa);
        sendLocalBroadcast(intent);
    }

    private void setCurrent(int currentMa){
        logger.debug("setting current to {}", currentMa);
        Intent intent = new Intent(VescDeviceSupport.COMMAND_SET_CURRENT);
        intent.putExtra(VescDeviceSupport.EXTRA_CURRENT, currentMa);
        sendLocalBroadcast(intent);
    }

    private void setRPM(int rpm){
        logger.debug("setting rpm to {}", rpm);
        Intent intent = new Intent(VescDeviceSupport.COMMAND_SET_RPM);
        intent.putExtra(VescDeviceSupport.EXTRA_RPM, rpm);
        sendLocalBroadcast(intent);
    }

    private void sendLocalBroadcast(Intent intent){
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return handleKeyPress(keyCode, false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return handleKeyPress(keyCode, true);
    }
}
