package nodomain.freeyourgadget.gadgetbridge.tasker.settings.activities;

import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import nodomain.freeyourgadget.gadgetbridge.R;

/**
 * Simple {@link EditTextPreference} with an button.
 * <p>
 * Exposes only {@link Button#setOnClickListener(View.OnClickListener)} and {@link Button#setText(int)}
 */
public class ButtonPreference extends EditTextPreference {

    private View.OnClickListener onClickListener;
    private int buttonTextResource;
    private boolean buttonDisabled;
    private Button button;
    private View parent;
    private boolean disableDialog;

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.button_preference_layout);
    }

    public ButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.button_preference_layout);
    }

    public ButtonPreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.button_preference_layout);
    }

    @Override
    protected void onBindView(final View view) {
        Button button = view.findViewById(R.id.tasker_button);
        parent = view;
        if (button != null) {
            this.button = button;
            button.setOnClickListener(onClickListener);
            button.setText(buttonTextResource);
            disableButton();
        }
        if (!disableDialog) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getDialog() != null && getDialog().isShowing()) {
                        return;
                    }
                    showDialog(null);
                }
            });
        }
        super.onBindView(view);
    }

    @Override
    protected void onClick() {
        super.onClick();
    }

    /**
     * Sets an {@link View.OnClickListener} to the button.
     *
     * @param clickListener
     */
    public void setOnClickListener(View.OnClickListener clickListener) {
        this.onClickListener = clickListener;
        if (button != null) {
            button.setOnClickListener(onClickListener);
        }
    }

    /**
     * Set button text with resource id.
     *
     * @param resourceId {@link R.string}
     */
    public void setButtonText(int resourceId) {
        buttonTextResource = resourceId;
        if (button != null) {
            button.setText(buttonTextResource);
        }
    }

    public void setButtonDisabled(boolean buttonDisabled) {
        this.buttonDisabled = buttonDisabled;
        if (button != null) {
            disableButton();
        }
    }

    public void setDisableDialog(boolean disableDialog) {
        this.disableDialog = disableDialog;
        if (parent != null) {
            parent.setOnClickListener(null);
        }
    }

    private void disableButton() {
        if (buttonDisabled) {
            button.setAlpha(.5f);
        } else {
            button.setAlpha(1f);
        }
        button.setClickable(!buttonDisabled);
    }

}
