package ru.mirea.danilov.data_thread;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.TimeUnit;
import ru.mirea.danilov.data_thread.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Runnable runn1 = () -> binding.tvInfo.setText("runOnUiThread: запущен первым");
        final Runnable runn2 = () -> binding.tvInfo.setText(binding.tvInfo.getText() + "\npost: запущен вторым");
        final Runnable runn3 = () -> binding.tvInfo.setText(binding.tvInfo.getText() + "\npostDelayed: запущен последним");

        Thread t = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                runOnUiThread(runn1);
                TimeUnit.SECONDS.sleep(1);
                binding.tvInfo.post(runn2);
                TimeUnit.SECONDS.sleep(2);
                binding.tvInfo.postDelayed(runn3, 1000);
            } catch (InterruptedException e) { e.printStackTrace(); }
        });
        t.start();
    }
}