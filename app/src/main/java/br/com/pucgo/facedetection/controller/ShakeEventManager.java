package br.com.pucgo.facedetection.controller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeEventManager implements SensorEventListener {

    private ShakeListener listener;
    private long shaketime = 0;

    private SensorManager sManager;
    private Sensor s;
    private float[] gravity = new float[]{0, 0, 0};

    // Counter for shake movements
    private int counter = 0;

    //Start time for the shake detection
    private long firstMovTime = 0;

    public void init(Context ctx) {
        sManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        s = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        register();
    }

    public void register() {
        sManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sManager.unregisterListener(this);
    }

    public void setOnShakeListener(ShakeListener listener) {
        this.listener = listener;
    }

    public interface ShakeListener {
        public void onShake();
    }

//The acceleration must be greater than a threshold level
//A fixed number of acceleration events must occur
//The time between these events must be in a fixed time window


    private float calcMaxAcceleration(SensorEvent event) {

        gravity[0] = calcGravityForce(event.values[0], 0);
        gravity[1] = calcGravityForce(event.values[1], 1);
        gravity[2] = calcGravityForce(event.values[2], 2);

        float accX = event.values[0] - gravity[0];  // Linear Acceleration
        float accY = event.values[1] - gravity[1];
        float accZ = event.values[2] - gravity[2];

        float max1 = Math.max(accX, accY);
        return Math.max(max1, accZ);
    }

    // Low pass filter
    private float calcGravityForce(float currentVal, int index) {
        float ALPHA = 0.8f;
        return ALPHA * gravity[index] + (1 - ALPHA) * currentVal;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float maxAcc = calcMaxAcceleration(sensorEvent);
        float MOV_THRESHOLD = 2.5f;
        if (maxAcc >= MOV_THRESHOLD) {
            if (counter == 0) {
                counter++;
                firstMovTime = System.currentTimeMillis();
                Log.i("SwA", "First mov..");
            } else {
                long now = System.currentTimeMillis();
                // Check if we're still in the shake window we defined
                int SHAKE_WINDOW_TIME_INTERVAL = 200;
                if ((now - firstMovTime) < SHAKE_WINDOW_TIME_INTERVAL)
                    counter++;
                else {
                    // Too much time has passed. Start over!
                    resetAllData();
                    return;
                }
                Log.i("SwA", "Mov counter [" + counter + "]");

                int MOV_COUNTS = 2;
                if (counter >= MOV_COUNTS) {
                    if (listener == null) Log.i("SwA", "it is NULL");
                    int SHAKE_INTERVAL = 5000;
                    if (listener != null && (now - shaketime) > SHAKE_INTERVAL) {
                        listener.onShake();
                        shaketime = System.currentTimeMillis();
                    }
                }
            }
        }

    }

    private void resetAllData() {
        firstMovTime = 0;
        counter = 0;

    }
}
