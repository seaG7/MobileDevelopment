package ru.mirea.danilov.work_manager;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploadWorker extends Worker {
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    @NonNull
    @Override
    public Result doWork() {
        Log.d("UploadWorker", "Начало долгой задачи...");
        try { Thread.sleep(300); } catch (InterruptedException e) { return Result.failure(); }
        Log.d("UploadWorker", "Задача успешно завершена!");
        return Result.success();
    }
}