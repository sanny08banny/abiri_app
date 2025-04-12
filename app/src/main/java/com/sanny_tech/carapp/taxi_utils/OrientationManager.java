package com.sanny_tech.carapp.taxi_utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationManager implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private long lastTimestamp = 0;
    private float[] lastAccelerometer = new float[3];
    private boolean accelerometerSet = false;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private OrientationListener listener;

    public interface OrientationListener {
        Void onOrientationChanged(float azimuth);
    }

    public OrientationManager(Context context, OrientationListener listener) {
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null){
            Log.e("Orientation manager", "Not found");
        }
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope == null){
            Log.e("Orientation manager", "Not found magnetometer");
        }
        SensorManager.getRotationMatrixFromVector(rotationMatrix,new float[3]);
    }

    public void startListening() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }else {
            Log.e("Orientation manager", "Tried to initialise null accelerometer");

        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }else {
            Log.e("Orientation manager", "Tried to initialise null");
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            accelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (lastTimestamp != 0) {
                final float dT = (event.timestamp - lastTimestamp) * 1e-9f;
                float[] deltaRotationVector = new float[4];
                if (dT - 0 < 1) {
                    float axisX = event.values[0];
                    float axisY = event.values[1];
                    float axisZ = event.values[2];

                    // Calculate angular speed from the gyroscope readings
                    float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                    if (omegaMagnitude > 0.1) {
                        axisX /= omegaMagnitude;
                        axisY /= omegaMagnitude;
                        axisZ /= omegaMagnitude;
                    }

                    // Integrate around this axis with the angular speed by the timestep
                    float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                    float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                    float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                    deltaRotationVector[0] = sinThetaOverTwo * axisX;
                    deltaRotationVector[1] = sinThetaOverTwo * axisY;
                    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                    deltaRotationVector[3] = cosThetaOverTwo;
                }
                SensorManager.getRotationMatrixFromVector(rotationMatrix, deltaRotationVector);
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                if (accelerometerSet) {
                    listener.onOrientationChanged(azimuthInDegrees);
                }
            }
            lastTimestamp = event.timestamp;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Optionally handle sensor accuracy changes here
    }
}
