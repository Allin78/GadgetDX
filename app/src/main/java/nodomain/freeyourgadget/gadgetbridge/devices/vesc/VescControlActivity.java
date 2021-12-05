package nodomain.freeyourgadget.gadgetbridge.devices.vesc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;

public class VescControlActivity extends AbstractGBActivity {
    private static final String TAG = "VescControlActivity";
    private boolean volumeKeyPressed = false;
    private boolean volumeKeysControl = false;
    private int currentRPM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vesc_control);
    }

    private boolean handleKeyPress(int keyCode, boolean isPressed){
        if(keyCode != 24 && keyCode != 25){
            return false;
        }

        if(volumeKeyPressed == isPressed){
            return true;
        }
        volumeKeyPressed = isPressed;

        Log.d(TAG, "volume " + (keyCode == 25 ? "down" : "up") + (isPressed ? " pressed" : " released"));
        if(!isPressed){
            setRPM(0);
            return true;
        }
        if(keyCode == 24){
            setRPM(currentRPM);
        }else{
            setBreakCurrent(0); // TODO: need to make break current adjustable
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

        View.OnTouchListener controlTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(v.getId() == R.id.vesc_control_button_fwd){
                        setRPM(VescControlActivity.this.currentRPM);
                    }else{
                        setBreakCurrent(0); // TODO: need to make break current settable
                    }
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    setRPM(0);
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

    }

    private void setRPM(int rpm){
        // STUB
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
