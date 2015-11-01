package com.mapinfo.newmap.Imle;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 方位传感器监听
 */
public class MyOrientationListener implements SensorEventListener {
    private SensorManager manager;
    private Context context;
    private Sensor sensor;
    private float lastX = 0;//记录的方向传感器X值
    onOrientationListener listener;

    public MyOrientationListener(Context context) {
        this.context = context;
    }

    public void start() {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager != null) {
            sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }

        if (sensor != null) {
            manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[SensorManager.DATA_X];

            if (Math.abs(lastX) > 0.1) {
                listener.onOrientationChanged(x);
            }
            lastX = x;
        }


    }

    public void setOrientationListener(onOrientationListener listener) {
        this.listener = listener;
    }

    public interface onOrientationListener {
        void onOrientationChanged(float x);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
