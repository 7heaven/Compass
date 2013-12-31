package com.example.compass;

/*
 *   _    ________    __    __    ________    ________    __    __    ________    ___   __ _
 *  /\\--/\______ \--/\ \--/\ \--/\  _____\--/\  ____ \--/\ \--/\ \--/\  _____\--/\  \-/\ \\\
 *  \ \\ \/_____/\ \ \ \ \_\_\ \ \ \ \____/_ \ \ \__/\ \ \ \ \_\ \ \ \ \ \____/_ \ \   \_\ \\\
 *   \ \\       \ \ \ \ \  ____ \ \ \  _____\ \ \  ____ \ \_ \ \_\ \  \ \  _____\ \ \  __   \\\
 *    \ \\       \ \ \ \ \ \__/\ \ \ \ \____/_ \ \ \__/\ \  \_ \ \ \   \ \ \____/_ \ \ \ \_  \\\
 *     \ \\       \ \_\ \ \_\ \ \_\ \ \_______\ \ \_\ \ \_\   \_ \_\    \ \_______\ \ \_\_ \__\\\
 *      \ \\       \/_/  \/_/  \/_/  \/_______/  \/_/  \/_/     \/_/     \/_______/  \/_/ \/__/ \\
 *       \ \\----------------------------------------------------------------------------------- \\
 *        \//                                                                                   \//
 *
 * 
 *
 */

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class BaseCompassView extends View implements SensorEventListener {

	private static final String TAG = "CompassView";
	
	private final float MAX_ROTATE_DEGREE = 1.0F;
	private float mDirection;
	private Handler mHandler = new Handler();
	private AccelerateInterpolator mInterpolator;
	private boolean mStopDrawing;
	private float mTargetDirection;
	private Sensor orientationSensor;
	private SensorManager sensorManager;

	protected float angle;

	protected int width, height;
	protected Point centerPoint;

	private Runnable rotateRunnable = new Runnable() {
        @Override
        public void run() {
        	if (mDirection != mTargetDirection && !mStopDrawing) {

                // calculate the short routine
                float to = mTargetDirection;
                if (to - mDirection > 180) {
                    to -= 360;
                } else if (to - mDirection < -180) {
                    to += 360;
                }

                // limit the max speed to MAX_ROTATE_DEGREE
                float distance = to - mDirection;
                if (Math.abs(distance) > MAX_ROTATE_DEGREE) {
                    distance = distance > 0 ? MAX_ROTATE_DEGREE : (-1.0f * MAX_ROTATE_DEGREE);
                }

                // need to slow down if the distance is short
                mDirection = normalizeDegree(mDirection + ((to - mDirection) * mInterpolator.getInterpolation(Math.abs(distance) > MAX_ROTATE_DEGREE ? 0.4f : 0.3f)));
                setAngle(mDirection);
            }

            mHandler.postDelayed(this, 20);
        }
    };
	
	public BaseCompassView(Context context) {
		this(context, null);
	}

	public BaseCompassView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BaseCompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		angle = 0.0F;
		
		centerPoint = new Point();

		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		mInterpolator = new AccelerateInterpolator();

	}
	
	public void onResume(){
		if(orientationSensor != null) sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mStopDrawing = false;
		mHandler.post(rotateRunnable);
	}
	
	public void onPause(){
		mStopDrawing = true;
		if(orientationSensor != null) sensorManager.unregisterListener(this);
		mHandler.removeCallbacks(rotateRunnable);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);

		centerPoint.x = width / 2;
		centerPoint.y = height / 2;
	}

	public void setAngle(float angle) {
		this.angle = angle;
		invalidate();
	}
	
	private float normalizeDegree(float degree) {
	    return (degree + 720) % 360;
	}

	// 使用完这个控件 退出前 要记得调用这个函数
	public void releaseSensor() {
		sensorManager.unregisterListener(this);
	}

	protected int getDegrees(double angle) {
		angle = Math.toDegrees(angle);
		return (int) (angle <= -90 && angle >= -180 ? 450 + angle : angle + 90);
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		float direction = event.values[0] * -1.0f;
        mTargetDirection = normalizeDegree(direction);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}