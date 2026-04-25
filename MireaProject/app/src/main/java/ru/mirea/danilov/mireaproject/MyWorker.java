package ru.mirea.danilov.mireaproject;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;

public class MyWorker extends Worker {
    public MyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("MireaProjectWorker", "Фоновая задача началась...");
        try {
            // Имитация долгой работы (например, сохранение данных)
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            return Result.failure();
        }
        Log.d("MireaProjectWorker", "Фоновая задача успешно завершена!");
        return Result.success();
    }
}