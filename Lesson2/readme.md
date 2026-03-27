# Отчёт по практической работе №2

**Подготовил:** Студент группы БСБО-09-23
**ФИО:** Данилов Михаил Алексеевич

---

## 1. Программные задачи и цели

Основная цель данной практической работы — глубокое изучение жизненного цикла активности (Activity) в ОС Android, а также освоение механизмов взаимодействия между компонентами приложения и операционной системой. В ходе работы требовалось изучить:

- Методы жизненного цикла Activity и их отслеживание через системный журнал (LogCat).
- Механизм намерений (Intents) для явного и неявного вызова активностей, а также передачи данных между ними.
- Способы информирования пользователя: всплывающие подсказки (Toast) и системные уведомления (Notifications) с учетом современных требований к разрешениям (Android 13+).
- Работу с модальными окнами через архитектуру фрагментов (`DialogFragment`), включая создание кастомных окон и использование стандартных (`TimePickerDialog`, `DatePickerDialog`, `ProgressDialog`).

## 2. Организация проекта

Разработка велась в рамках единого проекта, который был разделен на 6 независимых функциональных модулей:

1. `ActivityLifecycle` — отслеживание и логирование состояний жизненного цикла активности.
2. `MultiActivity` — работа с явными намерениями (Explicit Intents) и передача данных между экранами.
3. `IntentFilter` — использование неявных намерений (Implicit Intents) для вызова сторонних приложений (браузер, меню "Поделиться").
4. `ToastApp` — программный вызов всплывающих уведомлений с вычислением длины введенного текста.
5. `NotificationApp` — создание каналов уведомлений и отправка системных уведомлений в "шторку" устройства.
6. `Dialog` — конструирование диалоговых окон на базе `DialogFragment`.

---

## 3. Описание этапов разработки

### 3.1. Жизненный цикл Activity (модуль `ActivityLifecycle`)

В данном модуле была изучена последовательность вызова системных методов при создании, сворачивании, разворачивании и уничтожении активности. Для визуального отслеживания в каждый метод (`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`, а также методы сохранения состояния) было добавлено логирование с использованием класса `Log`.

Также был добавлен `EditText` для проверки того, как сохраняются введенные данные при различных манипуляциях с приложением (нажатие кнопок Home, Back).

**Листинг** `activity_main.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <EditText
        android:id="@+id/editTextText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:ems="10"
        android:inputType="text"
        android:text="tag"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.activitylifecycle;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
```

**Демонстрация работы (LogCat):**

> <img width="889" height="76" alt="image" src="https://github.com/user-attachments/assets/1a5fd075-2468-4a50-beda-0406c5f42e3c" />

### 3.2. Явные намерения и передача данных (модуль `MultiActivity`)

Цель модуля — научиться вызывать конкретную активность внутри своего приложения и передавать ей параметры. Была создана `SecondActivity`. В `MainActivity` считывался текст из `EditText`, добавлялся к строке с ФИО, запаковывался в объект `Intent` с помощью метода `putExtra` и передавался во вторую активность, где извлекался и отображался в `TextView`.

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.mulltiactivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "OnCreate()");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickNewActivity(View view) {
        EditText editText = findViewById(R.id.editTextData);
        String textToSend = editText.getText().toString();

        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("key", "РТУ МИРЭА - Данилов Михаил Алексеевич\n" + textToSend);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
```

**Листинг** `SecondActivity.java`:

```java
package ru.mirea.danilov.mulltiactivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.jetbrains.annotations.NotNull;

public class SecondActivity extends AppCompatActivity {

    private final String TAG = SecondActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        Log.i(TAG, "OnCreate()");

        TextView textView = findViewById(R.id.textViewResult);
        String text = getIntent().getStringExtra("key");
        if (text != null) {
            textView.setText(text);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }
}
```

**Демонстрация работы:**

> <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/0628da7e-7ec8-44c2-9cfa-8882b3735830" />
>  <img width="649" height="140" alt="image" src="https://github.com/user-attachments/assets/29b9f193-e525-4f04-856e-7048ab02729f" />
>  <img width="889" height="70" alt="image" src="https://github.com/user-attachments/assets/951f8345-5685-4caa-a19b-5946a56d2da1" />
>  <img width="891" height="89" alt="image" src="https://github.com/user-attachments/assets/eeea26ce-82fb-48dc-839d-02692ba4da37" />
>  <img width="982" height="44" alt="image" src="https://github.com/user-attachments/assets/9eb3b820-576d-4fbf-997e-b2eeb88eb898" />

### 3.3. Неявные намерения (модуль `IntentFilter`)

Была изучена возможность делегирования задач другим приложениям, установленным на устройстве. Реализованы две функции:

1. Открытие веб-страницы университета с использованием `ACTION_VIEW` и парсинга URI.
2. Передача текстовых данных (ФИО) в другие приложения (мессенджеры, почта) через окно выбора (`Intent.createChooser`) с использованием `ACTION_SEND`.

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.intentfilter;

import android.content.Intent;import android.net.Uri;import android.os.Bundle;import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onWebBrowserClick(View view) {
        Uri address = Uri.parse("https://www.mirea.ru/");
        Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openLinkIntent);
    }

    public void onShareClick(View view) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "РТУ МИРЭА");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Данилов Михаил Алексеевич");
        startActivity(Intent.createChooser(shareIntent, "Данилов Михаил Алексеевич"));
    }
}
```

**Демонстрация работы:**

>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/ef3dd1c7-26c7-4457-b3fe-8205c904704e" />
>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/85ebcb72-7b66-4536-8c3a-b2632534aa6d" />

### 3.4. Всплывающие уведомления (модуль `ToastApp`)

В модуле реализован подсчет количества символов, введенных пользователем в `EditText`. Результат вычислений форматируется в заданную строку ("СТУДЕНТ №... ГРУППА... Количество символов...") и выводится на экран в виде кратковременного сообщения с помощью класса `Toast`.

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.toastapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onCountClick(View view) {
        EditText editText = findViewById(R.id.editText);
        int length = editText.getText().toString().length();

        String message = "СТУДЕНТ № 5 ГРУППА БСБО-09-23 Количество символов - " + length;
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
```

**Демонстрация работы:**

> <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/75268d76-13e0-448b-bfcb-922eb57558d2" />

### 3.5. Системные уведомления (модуль `NotificationApp`)

Данный этап посвящен работе с `NotificationManager`. Была реализована отправка уведомления в системную панель (шторку) устройства. Учтены требования новых версий Android:

- Добавлено разрешение `POST_NOTIFICATIONS` в манифест.
- Реализована программная проверка и запрос разрешений у пользователя (для Android 13+).
- Создан канал уведомлений (Notification Channel), обязательный начиная с Android 8.0 (Oreo).

**Листинг** `AndroidManifest.xml` (Разрешения):

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.notificationapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PermissionCode = 200;
    private static final String CHANNEL_ID = "com.mirea.asd.notification.ANDROID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PermissionCode);
            }
        }
    }

    public void onClickSendNotification(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Danilov Mikhail Alekseevich Notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("MIREA Channel");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Congratulation!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line..."))
                .setContentTitle("Mirea");

        notificationManager.notify(1, builder.build());
    }
}
```

**Демонстрация работы:**

> 
>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/bd62f51a-3e4e-4d89-a595-7438712b09ff" />

### 3.6. Диалоговые окна и фрагменты (модуль `Dialog`)

Был изучен современный подход к созданию диалоговых окон через наследование от `DialogFragment`. В рамках модуля реализованы:

1. Кастомное окно `AlertDialog` с кнопками подтверждения, отмены и нейтрального действия, клики по которым обрабатываются в главной активности и выводят соответствующие `Toast` сообщения.
2. Стандартное окно выбора времени (`TimePickerDialog`).
3. Стандартное окно выбора даты (`DatePickerDialog`).
4. Окно процесса (`ProgressDialog`).

**Листинг** `MyDialogFragment.java` (кастомный AlertDialog):

```java
package ru.mirea.danilov.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class MyDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Здравствуй МИРЭА!")
                .setMessage("Успех близок?")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Иду дальше", (dialog, id) -> {
                    ((MainActivity)getActivity()).onOkClicked();
                    dialog.cancel();
                })
                .setNeutralButton("На паузе", (dialog, id) -> {
                    ((MainActivity)getActivity()).onNeutralClicked();
                    dialog.cancel();
                })
                .setNegativeButton("Нет", (dialog, id) -> {
                    ((MainActivity)getActivity()).onCancelClicked();
                    dialog.cancel();
                });
        return builder.create();
    }
}
```

**Листинг** `MyDateDialogFragment.java`:

```java
package ru.mirea.danilov.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class MyDateDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {}, year, month, day);
    }
}
```

**Листинг** `MyTimeDialogFragment.java`:

```java
package ru.mirea.danilov.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class MyTimeDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), (view, hourOfDay, minute1) -> {}, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}
```

**Листинг** `MyProgressDialogFragment.java`:

```java
package ru.mirea.danilov.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class MyProgressDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Загрузка");
        progressDialog.setMessage("Пожалуйста, подождите...");
        progressDialog.setIndeterminate(true);
        return progressDialog;
    }
}
```

**Листинг** `MainActivity.java`:

```java
package ru.mirea.danilov.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void onClickShowDialog(View view) {
        MyDialogFragment dialogFragment = new MyDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "mirea");
    }

    public void onOkClicked() {
        Toast.makeText(getApplicationContext(), "Вы выбрали кнопку \"Иду дальше\"!", Toast.LENGTH_LONG).show();
    }

    public void onCancelClicked() {
        Toast.makeText(getApplicationContext(), "Вы выбрали кнопку \"Нет\"!", Toast.LENGTH_LONG).show();
    }

    public void onNeutralClicked() {
        Toast.makeText(getApplicationContext(), "Вы выбрали кнопку \"На паузе\"!", Toast.LENGTH_LONG).show();
    }

    public void onClickTimeDialog(View view) {
        new MyTimeDialogFragment().show(getSupportFragmentManager(), "timePicker");
    }

    public void onClickDateDialog(View view) {
        new MyDateDialogFragment().show(getSupportFragmentManager(), "datePicker");
    }

    public void onClickProgressDialog(View view) {
        new MyProgressDialogFragment().show(getSupportFragmentManager(), "progressDialog");
    }
}
```

**Демонстрация работы:**

> 
>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/990d6f4a-074f-44f5-a16f-8db79dd7120a" />
>
>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/8b0b700e-cdb5-4a39-8b15-f02c1fcaab08" />
>
>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/a397ecac-41d2-479a-b0b2-755e7371d4f0" />
>
>  <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/1dd26f91-eed6-4215-a2b3-e806775568e5" />
> 
>  <img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/93949c1d-1d93-4872-bae0-0fcc4e52ac01" />

---

## 4. Итоги практической работы

По завершении второй практической работы были достигнуты следующие результаты:

- Детально изучен жизненный цикл Android-приложений, что позволяет корректно управлять ресурсами и сохранять пользовательские данные при сворачивании или повороте экрана.
- Освоены механизмы навигации и передачи данных между активностями.
- Получен навык интеграции приложения в экосистему ОС Android посредством использования системных фильтров (Implicit Intents) для открытия ссылок и шеринга информации.
- Изучены и применены на практике три основных вида информирования пользователя: Toasts, системные Notifications с поддержкой современных API и модальные окна Dialogs на базе архитектуры фрагментов.
- Все разработанные модули успешно отлажены и протестированы на эмуляторе.


