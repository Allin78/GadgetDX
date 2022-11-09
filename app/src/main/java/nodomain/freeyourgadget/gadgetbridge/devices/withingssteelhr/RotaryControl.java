package nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import nodomain.freeyourgadget.gadgetbridge.R;

/**
 * Lots of this code has been provided by Milos Marinkovic here https://github.com/milosmns/circular-slider-android.
 * I just simplified things a little for the needs in GB.
 */
public class RotaryControl extends View {

    public interface RotationListener {
        void onRotation(double pos);
    }

    private int controlPointX;
    private int controlPointY;

    private int controlCenterX;
    private int controlCenterY;
    private int controlRadius;

    private int padding;
    private int controlPointSize;
    private int controlPointColor;
    private int lineColor;
    private int lineThickness;
    private double startAngle;
    private double angle ;
    private boolean isControlPointSelected = false;
    private Paint paint = new Paint();
    private RotationListener rotationListener;

    public RotaryControl(Context context) {
        this(context, null);
    }

    public RotaryControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotaryControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotaryControl, defStyleAttr, 0);

        startAngle = a.getFloat(R.styleable.RotaryControl_start_angle, (float) Math.PI / 2);
        angle = a.getFloat(R.styleable.RotaryControl_angle, (float) Math.PI / 2);
        controlPointSize = a.getDimensionPixelSize(R.styleable.RotaryControl_controlpoint_size, 50);
        controlPointColor = a.getColor(R.styleable.RotaryControl_controlpoint_color, Color.GRAY);
        lineThickness = a.getDimensionPixelSize(R.styleable.RotaryControl_line_thickness, 20);
        lineColor = a.getColor(R.styleable.RotaryControl_line_color, Color.RED);
        calculateAndSetPadding();
        a.recycle();
    }

    private void calculateAndSetPadding() {
        int totalPadding = getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop() + getPaddingEnd() + getPaddingStart();
        padding = totalPadding / 6;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        int smallerDim = width > height ? height : width;
        int largestCenteredSquareLeft = (width - smallerDim) / 2;
        int largestCenteredSquareTop = (height - smallerDim) / 2;
        int largestCenteredSquareRight = largestCenteredSquareLeft + smallerDim;
        int largestCenteredSquareBottom = largestCenteredSquareTop + smallerDim;
        controlCenterX = largestCenteredSquareRight / 2 + (width - largestCenteredSquareRight) / 2;
        controlCenterY = largestCenteredSquareBottom / 2 + (height - largestCenteredSquareBottom) / 2;
        controlRadius = smallerDim / 2 - lineThickness / 2 - padding;

        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawRotationCircle(canvas);
        drawControlPoint(canvas);
    }

    private void drawControlPoint(Canvas canvas) {
        controlPointX = (int) (controlCenterX + controlRadius * Math.cos(angle));
        controlPointY = (int) (controlCenterY - controlRadius * Math.sin(angle));
        paint.setColor(controlPointColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(controlPointX, controlPointY, controlPointSize, paint);
    }

    private void drawRotationCircle(Canvas canvas) {
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineThickness);
        paint.setAntiAlias(true);
        canvas.drawCircle(controlCenterX, controlCenterY, controlRadius, paint);
    }

    private void updateRotationPosition(int touchX, int touchY) {
        int distanceX = touchX - controlCenterX;
        int distanceY = controlCenterY - touchY;
        double c = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        angle = Math.acos(distanceX / c);

        if (distanceY < 0) {
            angle = -angle;
        }

        if (rotationListener != null) {
            rotationListener.onRotation((angle - startAngle) / (2 * Math.PI));
        }
    }

    public void setRotationListener(RotationListener listener) {
        rotationListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                if (x < controlPointX + controlPointSize && x > controlPointX - controlPointSize && y < controlPointY + controlPointSize && y > controlPointY - controlPointSize) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isControlPointSelected = true;
                    updateRotationPosition(x, y);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isControlPointSelected) {
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    updateRotationPosition(x, y);
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                getParent().requestDisallowInterceptTouchEvent(false);
                isControlPointSelected = false;
                break;
            }
        }

        invalidate();
        return true;
    }
}
