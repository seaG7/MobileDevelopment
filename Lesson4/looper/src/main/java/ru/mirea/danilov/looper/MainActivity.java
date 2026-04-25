package ru.mirea.danilov.looper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import ru.mirea.danilov.looper.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MyLooper myLooper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Handler mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("MainActivity", "Результат из Looper: " + msg.getData().getString("result"));
            }
        };

        myLooper = new MyLooper(mainHandler);
        myLooper.start();

        binding.buttonMirea.setOnClickListener(v -> {
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("JOB", binding.editTextMirea.getText().toString());
            bundle.putInt("AGE", Integer.parseInt(binding.editAge.getText().toString()));
            msg.setData(bundle);
            myLooper.mHandler.sendMessage(msg);
        });
    }
}