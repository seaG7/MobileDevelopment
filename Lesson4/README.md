# Отчёт по практической работе №4

**Подготовил:** Студент группы БСБО-09-23 **ФИО:** Данилов Михаил Алексеевич

---

## 1. Программные задачи и цели

Основная цель данной практической работы — освоение технологий асинхронного выполнения кода, управления жизненным циклом фоновых процессов и проектирования отказоустойчивых фоновых задач в среде Android. В рамках работы ставились следующие задачи:

- Изучение архитектуры многопоточности Android, понимание роли Main Thread (UI-потока) и предотвращение ошибок типа ANR (Application Not Responding).
- Освоение низкоуровневых механизмов работы с потоками (`Thread`, `Runnable`) и приоритетами выполнения в ОС Linux/Android.
- Изучение способов безопасного взаимодействия между рабочими потоками и интерфейсом пользователя через `Handler`, `Looper` и специализированные методы `View`.
- Реализация асинхронной обработки данных с помощью загрузчиков (`AsyncTaskLoader`), обеспечивающих сохранение данных при смене конфигурации устройства.
- Практическое применение криптографических стандартов (симметричное шифрование AES) для защиты передаваемой информации.
- Разработка системных служб (`Services`), понимание разницы между фоновыми службами и службами переднего плана (Foreground Services).
- Изучение и внедрение библиотеки `WorkManager` как современного стандарта для выполнения отложенных и гарантированных задач с учетом системных ограничений (заряд батареи, сеть).

## 2. Организация проекта

Разработка велась в рамках комплексного проекта, состоящего из модулей, каждый из которых изолированно решает конкретную технологическую задачу:

1. **thread** — Базовая работа с потоками и математические вычисления в фоне.
2. **data_thread** — Механизмы обновления UI из не-UI потоков.
3. **looper** — Передача сообщений между потоками через Handler.
4. **cryptoloader** — Асинхронная загрузка с применением шифрования.
5. **service_app** — Реализация фонового плеера с системным уведомлением.
6. **work_manager** — Планирование задач через Constraints.
7. **MireaProject** — Интеграция фонового выполнения в структуру Navigation Drawer.

---

## 3. Описание этапов разработки

### 3.1. Управление именами и приоритетами потоков (модуль `thread`)

В этом модуле реализовано управление свойствами главного потока. Изменение имени потока (`setName`) используется для идентификации логов и отладки сложных систем. Также реализована логика вычисления среднего количества пар в месяц: чтобы не блокировать интерфейс при вводе данных, вычисления производятся в новом потоке.

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView android:id="@+id/textViewInfo"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Инфо о потоке" />

    <EditText android:id="@+id/editTotalPairs"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Общее количество пар"
        android:inputType="number" />

    <EditText android:id="@+id/editTotalDays"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Количество учебных дней"
        android:inputType="number" />

    <Button android:id="@+id/buttonCalculate"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Посчитать среднее в фоне" />

    <TextView android:id="@+id/textViewResult"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="18sp"
        android:text="Результат появится здесь" />
</LinearLayout>
```

**Листинг** `MainActivity.java`:

```java
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
```

**Демонстрация работы:**

> ![alt text](image_2026-04-24_22-26-24.png)*Рисунок 1: Главный экран модуля thread (имя потока обновлено на данные студента)*

### 3.2. Синхронизация потоков с UI (модуль `data_thread`)

В модуле детально разобраны три способа "проброса" задачи в главный поток: `runOnUiThread`, `post` и `postDelayed`. Это критически важно, так как попытка напрямую изменить `TextView` из фонового потока приведет к `CalledFromWrongThreadException`.

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView android:id="@+id/tvInfo"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Ожидание..."
        android:textSize="20sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:

```java
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
```

**Демонстрация работы:**

> ![alt text](image_2026-04-24_22-35-24.png)*Рисунок 3: Экран с последовательным изменением текста (имитация асинхронных ответов)*

### 3.3. Реализация Message Loop (модуль `looper`)

Создан кастомный поток `MyLooper`, который не завершается после выполнения `run()`, а переходит в состояние ожидания сообщений благодаря `Looper.prepare()` и `Looper.loop()`. В качестве практической задачи реализована имитация "тяжелой" работы: поток принимает профессию и возраст пользователя, засыпает на время, зависящее от возраста, и возвращает результат в главный поток.

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <EditText android:id="@+id/editTextMirea" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Ваша работа"/>
    <EditText android:id="@+id/editAge" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Ваш возраст" android:inputType="number"/>
    <Button android:id="@+id/buttonMirea" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Отправить в Looper"/>
</LinearLayout>
```

**Листинг** `MyLooper.java`:

```java
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
```

**Листинг** `MainActivity.java`:

```java
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
```

**Демонстрация работы:**

![alt text](image_2026-04-24_22-39-33.png)*Рисунок 4: Заполнение формы (возраст и профессия)*
> 
![alt text](image_2026-04-24_22-40-05.png)
> *Рисунок 5: Логи лопера, показывающие обработку переданного объекта Message*

### 3.4. Безопасная обработка данных в Loader (модуль `cryptoloader`)

Здесь применен `AsyncTaskLoader` для дешифрования данных. Использование загрузчика позволяет избежать утечек памяти и перезапуска задачи при повороте экрана. Для безопасности данных применен алгоритм AES-256: строка шифруется в активности с помощью сгенерированного ключа, передается через `Bundle` в загрузчик, где происходит обратный процесс.

**Листинг** `build.gradle` (Модуль cryptoloader):

```gradle
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.mirea.danilov.cryptoloader"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.mirea.danilov.cryptoloader"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <EditText android:id="@+id/editTextMirea" android:layout_width="match_parent" android:layout_height="wrap_content" android:hint="Введите фразу"/>
    <Button android:id="@+id/buttonMirea" android:layout_width="match_parent" android:layout_height="wrap_content" android:text="Шифровать и загрузить"/>
</LinearLayout>
```

**Листинг** `MyLoader.java`:

```java
package ru.mirea.danilov.cryptoloader;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MyLoader extends AsyncTaskLoader<String> {
    private Bundle args;
    public MyLoader(@NonNull Context context, Bundle args) {
        super(context);
        this.args = args;
    }
    @Override
    protected void onStartLoading() { super.onStartLoading(); forceLoad(); }

    @Override
    public String loadInBackground() {
        byte[] cryptText = args.getByteArray("word");
        byte[] key = args.getByteArray("key");
        try {
            SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, originalKey);
            return new String(cipher.doFinal(cryptText));
        } catch (Exception e) { return "Ошибка дешифрования"; }
    }
}
```

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.cryptoloader;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import javax.crypto.SecretKey;
import ru.mirea.danilov.cryptoloader.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private ActivityMainBinding binding;
    private final int LoaderID = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonMirea.setOnClickListener(v -> {
            SecretKey key = generateKey();
            byte[] shiper = encryptMsg(binding.editTextMirea.getText().toString(), key);
            Bundle bundle = new Bundle();
            bundle.putByteArray("word", shiper);
            bundle.putByteArray("key", key.getEncoded());
            LoaderManager.getInstance(this).initLoader(LoaderID, bundle, this);
        });
    }

    public static SecretKey generateKey() {
        try {
            javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance("AES");
            kg.init(256);
            return kg.generateKey();
        } catch (Exception e) { return null; }
    }

    public static byte[] encryptMsg(String message, SecretKey secret) {
        try {
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
            c.init(javax.crypto.Cipher.ENCRYPT_MODE, secret);
            return c.doFinal(message.getBytes());
        } catch (Exception e) { return null; }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyLoader(this, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        Toast.makeText(this, "Дешифровано: " + data, Toast.LENGTH_LONG).show();
        LoaderManager.getInstance(this).destroyLoader(LoaderID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}
}
```

**Демонстрация работы:**
![alt text](image_2026-04-24_22-41-10.png)
> *Рисунок 6: Ввод секретной фразы и её успешная расшифровка в Toast*

### 3.5. Службы переднего плана (модуль `service_app`)

Реализован музыкальный плеер на базе `Foreground Service`. Основная особенность в том, что такая служба продолжает работать даже после того, как пользователь смахнул приложение из списка запущенных. Для соблюдения требований Android Oreo+ реализован `NotificationChannel`, а в манифесте указан тип сервиса `mediaPlayback`.

**Листинг** `AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Lesson4">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <service android:name=".PlayerService" android:foregroundServiceType="mediaPlayback" android:enabled="true" android:exported="false" />
    </application>

</manifest>
```

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical">
    <Button android:id="@+id/buttonPlay" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Play"/>
    <Button android:id="@+id/buttonStop" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Stop"/>
</LinearLayout>
```

**Листинг** `PlayerService.java`:

```java
package ru.mirea.danilov.service_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import ru.mirea.danilov.service_app.R;

public class PlayerService extends Service {
    private MediaPlayer mediaPlayer;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Service", NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Музыкальный плеер")
                .setContentText("Играет The Weeknd - Timeless")
                .setSmallIcon(android.R.drawable.ic_media_play);

        startForeground(1, builder.build());
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
```

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.service_app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import ru.mirea.danilov.service_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 200);
            }
        }

        binding.buttonPlay.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerService.class);
            ContextCompat.startForegroundService(this, intent);
        });

        binding.buttonStop.setOnClickListener(v -> {
            stopService(new Intent(this, PlayerService.class));
        });
    }
}
```

**Демонстрация работы:**

> ![alt text](image_2026-04-25_15-23-54.png)*Рисунок 7: Экран управления плеером (Play/Stop)* 
> ![alt text](image_2026-04-24_23-22-47.png)
> *Рисунок 8: Активное уведомление в шторке с фамилией студента*

### 3.6. Гарантированное выполнение задач (модуль `work_manager`)

В данном модуле продемонстрирована работа с `WorkManager`. В отличие от обычных потоков, `Worker` может быть запущен системой даже после перезагрузки устройства. Настроены `Constraints`: задача (логгирование) запускается только при наличии активного интернет-соединения.

**Листинг** `build.gradle` (Модуль work_manager):

```gradle
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.mirea.danilov.workmanager"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "ru.mirea.danilov.workmanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.work:work-runtime:2.8.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center">
    <Button android:id="@+id/buttonStart" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Запустить Worker"/>
</LinearLayout>
```

**Листинг** `UploadWorker.java`:

```java
package ru.mirea.danilov.workmanager;

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
```

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.workmanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import ru.mirea.danilov.workmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonStart.setOnClickListener(v -> {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadWorker.class)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(this).enqueue(request);
        });
    }
}
```

**Демонстрация работы:**

> ![alt text](image_2026-04-24_23-44-32.png)*Рисунок 9: Постановка задачи в очередь WorkManager*

### 3.7. Контрольное задание: Интеграция в MireaProject

В основной проект `MireaProject` внедрен функционал фоновой работы. Был создан новый фрагмент `WorkerFragment`, который был добавлен в `Navigation Drawer` и `NavGraph`. По нажатию кнопки во фрагменте создается `OneTimeWorkRequest` для фоновой задачи. Это имитирует реальный сценарий, когда приложение выполняет синхронизацию или очистку кэша в фоновом режиме по запросу пользователя.

**Листинг** `mobile_navigation.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/mobile_navigation"
    app:startDestination="@+id/nav_data">

    <fragment
        android:id="@+id/nav_data"
        android:name="ru.mirea.danilov.mireaproject.DataFragment"
        android:label="Отрасль"
        tools:layout="@layout/fragment_data" />

    <fragment
        android:id="@+id/nav_webview"
        android:name="ru.mirea.danilov.mireaproject.WebViewFragment"
        android:label="Браузер"
        tools:layout="@layout/fragment_web_view" />
    <fragment
        android:id="@+id/nav_worker"
        android:name="ru.mirea.danilov.mireaproject.WorkerFragment"
        android:label="Фоновая задача"
        tools:layout="@layout/fragment_worker" />
</navigation>
```

**Листинг** `activity_main_drawer.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools" tools:showIn="navigation_view">
    <group android:checkableBehavior="single">
        <item android:id="@+id/nav_data" android:title="Отрасль" />
        <item android:id="@+id/nav_webview" android:title="Браузер" />
        <item android:id="@+id/nav_worker" android:title="Фоновая задача (Worker)" />
    </group>
</menu>
```

**Листинг** `fragment_worker.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Демонстрация работы WorkManager"
        android:textSize="18sp"
        android:layout_marginBottom="20dp"/>

    <Button
        android:id="@+id/btnStartWorker"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Запустить фоновую задачу"/>
</LinearLayout>
```

**Листинг** `MyWorker.java`:

```java
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
```

**Листинг** `WorkerFragment.java`:

```java
package ru.mirea.danilov.mireaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import ru.mirea.danilov.mireaproject.databinding.FragmentWorkerBinding;

public class WorkerFragment extends Fragment {
    private FragmentWorkerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkerBinding.inflate(inflater, container, false);

        binding.btnStartWorker.setOnClickListener(v -> {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWorker.class).build();
            WorkManager.getInstance(requireContext()).enqueue(workRequest);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

**Листинг** `MainActivity.java` (связывание навигации):

```java
package ru.mirea.danilov.mireaproject;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;

import ru.mirea.danilov.mireaproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_data, R.id.nav_webview)
                .setOpenableLayout(drawer)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
```

**Демонстрация работы:**

> ![alt text](image_2026-04-25_15-26-44-1.png)
> *Рисунок 10: Новый пункт "Фоновая задача" в боковом меню MireaProject*
> ![alt text](image_2026-04-25_15-26-53-1.png)
> *Рисунок 11: Окно запуска фоновой задачи*
> !![alt text](image_2026-04-25_15-27-10.png)
> *Рисунок 12: Работа фрагмента и лог подтверждения выполнения MyWorker*

---

## 4. Итоги практической работы

В ходе работы были полностью реализованы все поставленные задачи. Главным итогом стало понимание того, что любая операция, занимающая более 16-20 мс, должна выноситься из главного потока.

Основные выводы:

1. `Thread` и `Handler` подходят для быстрых, простых операций внутри жизненного цикла активности.
2. `Services` необходимы для длительных процессов, не зависящих от UI (например, музыка или GPS-трекинг).
3. `WorkManager` — наиболее предпочтительный способ для задач, которые должны выполниться в будущем независимо от состояния приложения.
4. Правильное использование асинхронности напрямую влияет на плавность интерфейса и пользовательский опыт.


