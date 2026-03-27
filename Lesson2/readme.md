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

> 
>  ![](/api/attachments.redirect?id=97b4909e-1737-4312-9299-d8244d6f45f6 "aspect=1")

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

> 
>  ![](/api/attachments.redirect?id=8dcdb5ff-1c9b-4738-8335-3a237e01bb2b "aspect=0.5089799272965867")
>
>  ![](/api/attachments.redirect?id=95ebbdc3-3ed7-4102-a745-276759674b74 "aspect=0.9970059751664426")
>
> 
>  ![](/api/attachments.redirect?id=696fc721-c7d6-4182-8d1a-b18d773f18df "aspect=1")
>
> 
>  ![](/api/attachments.redirect?id=a8d80419-427e-46a3-8b80-28c7b3637085 "aspect=1")
>
> 
>  ![](/api/attachments.redirect?id=c7e321d6-5eef-4405-b14a-3c5a1e4cd3a6 "aspect=1")

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

> 
>  ![](/api/attachments.redirect?id=41cf16bd-5fbf-43eb-81bf-c6b10b440c19 "aspect=0.4461054057918779")
>
>  ![](/api/attachments.redirect?id=bc7a7d05-17a2-4d4a-8210-c30ab4616b22 "aspect=0.4431113809583202")

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

> 
>  ![](/api/attachments.redirect?id=ec49fe99-d316-45b8-9ee3-55eb7f9f4d6f "aspect=0.4550874802925496")

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
>  ![](/api/attachments.redirect?id=82f704f6-6312-4e1f-aea9-885831de814d "aspect=0.4011950332885166")

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
>  ![](/api/attachments.redirect?id=1aae7931-faa4-4986-ab48-c1ddece11dc8 "aspect=0.36526673528582765")
>
>  ![](/api/attachments.redirect?id=383c20d6-16c7-43e7-95f5-b535ba404257 "aspect=0.3712547849529424")
>
>  ![](/api/attachments.redirect?id=9383657e-c473-492f-a0ca-d9aff82a28bd "aspect=0.3772428346200566")
>
>  ![](/api/attachments.redirect?id=8c6f925c-3c93-4f8e-80e6-4fe37e9eb8d5 "aspect=0.3712547849529422")
>
>  ![](/api/attachments.redirect?id=7fdf3fdb-49ae-459d-b5ff-cfa4d772d987 "aspect=0.3742488097865")

---

## 4. Итоги практической работы

По завершении второй практической работы были достигнуты следующие результаты:

- Детально изучен жизненный цикл Android-приложений, что позволяет корректно управлять ресурсами и сохранять пользовательские данные при сворачивании или повороте экрана.
- Освоены механизмы навигации и передачи данных между активностями.
- Получен навык интеграции приложения в экосистему ОС Android посредством использования системных фильтров (Implicit Intents) для открытия ссылок и шеринга информации.
- Изучены и применены на практике три основных вида информирования пользователя: Toasts, системные Notifications с поддержкой современных API и модальные окна Dialogs на базе архитектуры фрагментов.
- Все разработанные модули успешно отлажены и протестированы на эмуляторе.


