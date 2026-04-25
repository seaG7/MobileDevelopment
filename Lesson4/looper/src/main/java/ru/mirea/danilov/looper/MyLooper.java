package ru.mirea.danilov.looper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MyLooper extends Thread {
    public Handler mHandler;
    private Handler mainHandler;

    public MyLooper(Handler mainThreadHandler) {
        mainHandler = mainThreadHandler;
    }

    @Override
    public void run() {
        Log.d("MyLooper", "run");
        Looper.prepare();
        mHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                String job = msg.getData().getString("JOB");
                int age = msg.getData().getInt("AGE");
                try { Thread.sleep(age * 100L); } catch (InterruptedException e) { e.printStackTrace(); }

                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("result", String.format("Вы работаете как %s. Задержка была %d мс", job, age*100));
                message.setData(bundle);
                mainHandler.sendMessage(message);
            }
        };
        Looper.loop();
    }
}