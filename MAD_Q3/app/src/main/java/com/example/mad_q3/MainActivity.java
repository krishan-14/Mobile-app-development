package com.example.mad_q3; // <-- CHECK THIS MATCHES YOUR PACKAGE NAME

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accel, light, prox;
    private TextView tvX, tvY, tvZ, tvLight, tvProx, tvProxStatus;
    private LinearProgressIndicator lightProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvX = findViewById(R.id.tvX); tvY = findViewById(R.id.tvY); tvZ = findViewById(R.id.tvZ);
        tvLight = findViewById(R.id.tvLight); lightProgress = findViewById(R.id.lightProgress);
        tvProx = findViewById(R.id.tvProx); tvProxStatus = findViewById(R.id.tvProxStatus);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            tvX.setText(String.format(Locale.US, "%.2f", event.values[0]));
            tvY.setText(String.format(Locale.US, "%.2f", event.values[1]));
            tvZ.setText(String.format(Locale.US, "%.2f", event.values[2]));
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float val = event.values[0];
            tvLight.setText(String.format(Locale.US, "%.0f lux", val));
            lightProgress.setProgress((int) Math.min(val / 10, 100), true);
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float dist = event.values[0];
            tvProx.setText(String.format(Locale.US, "%.0f cm", dist));
            if (dist < 5) {
                tvProxStatus.setText("OBJECT NEAR");
                tvProxStatus.setTextColor(Color.RED);
            } else {
                tvProxStatus.setText("No object near");
                tvProxStatus.setTextColor(Color.parseColor("#34A853"));
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, prox, SensorManager.SENSOR_DELAY_UI);
    }

    @Override protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}