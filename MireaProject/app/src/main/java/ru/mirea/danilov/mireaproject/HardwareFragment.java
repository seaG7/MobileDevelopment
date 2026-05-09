package ru.mirea.danilov.mireaproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.mirea.danilov.mireaproject.databinding.FragmentHardwareBinding;

public class HardwareFragment extends Fragment implements SensorEventListener {

    private FragmentHardwareBinding binding;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHardwareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        sensorManager = (SensorManager) requireActivity()
                .getSystemService(Context.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            binding.lightValueTextView.setText("Датчик освещенности отсутствует");
            binding.recommendationTextView.setText("На этом устройстве невозможно выполнить проверку освещенности.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (lightSensor != null) {
            sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float light = event.values[0];

            binding.lightValueTextView.setText("Освещенность: " + light + " лк");

            if (light < 100) {
                binding.recommendationTextView.setText(
                        "Слишком темно. Лучше включить дополнительный источник света."
                );
            } else if (light <= 500) {
                binding.recommendationTextView.setText(
                        "Освещение нормальное. Условия подходят для работы."
                );
            } else {
                binding.recommendationTextView.setText(
                        "Слишком ярко. Лучше уменьшить яркость или отойти от прямого света."
                );
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}