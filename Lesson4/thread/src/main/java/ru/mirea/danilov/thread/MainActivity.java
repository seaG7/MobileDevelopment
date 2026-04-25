package ru.mirea.danilov.thread;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import ru.mirea.danilov.thread.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Thread mainThread = Thread.currentThread();
        binding.textViewInfo.setText("Текущий поток: " + mainThread.getName());
        mainThread.setName("МОЙ НОМЕР ГРУППЫ: БСБО-09-23, НОМЕР ПО СПИСКУ: 5");
        binding.textViewInfo.append("\n" + mainThread.getName());

        binding.buttonCalculate.setOnClickListener(v -> {
            new Thread(() -> {
                int numberThread = counter++;
                Log.d("ThreadProject", "Запущен поток № " + numberThread);
                try {
                    String sPairs = binding.editTotalPairs.getText().toString();
                    String sDays = binding.editTotalDays.getText().toString();

                    if (!sPairs.isEmpty() && !sDays.isEmpty()) {
                        float result = Float.parseFloat(sPairs) / Float.parseFloat(sDays);
                        runOnUiThread(() -> binding.textViewResult.setText("Среднее пар в день: " + result));
                    }
                } catch (Exception e) {
                    Log.e("ThreadProject", "Error: " + e.getMessage());
                }
                Log.d("ThreadProject", "Выполнен поток № " + numberThread);
            }).start();
        });
    }
}