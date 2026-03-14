# Отчёт по практической работе №1

**Подготовил:** Студент группы БСБО-09-23  
**ФИО:** Данилов Михаил Алексеевич  

---

## 1. Программные задачи и цели
Основная цель — освоение среды Android Studio и инструментов разработки мобильного ПО. В ходе работы требовалось изучить файловую иерархию проекта, методы верстки интерфейсов через различные контейнеры (Layouts), управление ресурсами, а также программную обработку взаимодействий пользователя с элементами экрана на языке Java.

## 2. Организация проекта
Разработка велась в рамках единого проекта, разделенного на три функциональных модуля:
1. **`layouttype`** — отработка навыков использования `LinearLayout`, `TableLayout` и `ConstrainedLayout`.
2. **`control_lesson1`** — создание сложных интерфейсов через `ConstraintLayout` и поддержка различных ориентаций дисплея.
3. **`buttonclicker`** — программирование логики кнопок и работа с состояниями виджетов.

Дополнительно была выполнена конфигурация виртуального устройства (AVD) для отладки приложений.

---

## 3. Описание этапов разработки

### 3.1. Работа с базовой разметкой (модуль `layouttype`)

#### Линейная компоновка (LinearLayout)
Был спроектирован интерфейс `linear_layout.xml`, базирующийся на вложенности контейнеров. Основной вертикальный стек содержит два горизонтальных ряда кнопок. Чтобы элементы распределялись по ширине равномерно, использован механизм весов (`android:layout_weight="1"`), что позволяет кнопкам динамически подстраиваться под размер экрана.

**Листинг `linear_layout.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <Button
            android:id="@+id/button3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <Button
            android:id="@+id/button2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <Button
            android:id="@+id/button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <Button
            android:id="@+id/button4"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <Button
            android:id="@+id/button5"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <Button
            android:id="@+id/button6"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />
    </LinearLayout>
</LinearLayout>
```

#### Табличная компоновка (TableLayout)
В файле `table_layout.xml` реализована сетка элементов. Использование строк `TableRow` позволило разместить в ячейках разнородные объекты: текстовые подписи, стандартные кнопки, `CheckBox` и `ImageButton` с иконкой выключения из системных ресурсов Android.

**Листинг `table_layout.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<TableLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TableRow
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <Button
            android:id="@+id/button8"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <TextView
            android:id="@+id/textView3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="This is Table View!" />

        <Button
            android:id="@+id/button7"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />
    </TableRow>

    <TableRow
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <Button
            android:id="@+id/button9"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <CheckBox
            android:id="@+id/checkBox"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="CheckBox" />

    </TableRow>

    <TableRow
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <ImageButton
            android:id="@+id/imageButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            app:srcCompat="@android:drawable/ic_lock_power_off" />

        <Button
            android:id="@+id/button10"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />

        <Button
            android:id="@+id/button11"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Button" />
    </TableRow>
</TableLayout>
```

### 3.2. Верстка сложного интерфейса (модуль `control_lesson1`)

#### Профиль пользователя мессенджера
Для создания макета `activity_main.xml` использовался `ConstraintLayout`. Это позволило гибко связать элементы (фотографию, текстовые блоки с именем и организацией, кнопку сохранения) между собой. Привязки (Constraints) гарантируют корректное отображение элементов на экранах с разным разрешением.

**Листинг `activity_main.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="yamishadanilov"
        android:textSize="34sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/imageView"
        app:layout_constraintVertical_bias="0.0" />

    <ImageView
        android:id="@+id/imageView3"
        android:layout_width="413dp"
        android:layout_height="269dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.9"
        app:srcCompat="@drawable/gray_background" />

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="103dp"
        android:layout_height="161dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.498"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.026"
        app:srcCompat="@drawable/avatar" />

    <TextView
        android:id="@+id/textView2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="+7 (985)111-11-11"
        android:textSize="20sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/textView"
        app:layout_constraintVertical_bias="0.022" />

    <EditText
        android:id="@+id/editTextText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:ems="10"
        android:inputType="text"
        android:text="Bio"
        android:textSize="20sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.502"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="@+id/imageView3"
        app:layout_constraintVertical_bias="0.112" />

    <EditText
        android:id="@+id/editTextDate"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:ems="10"
        android:inputType="date"
        android:text="Date"
        android:textSize="20sp"
        app:layout_constraintBottom_toBottomOf="@+id/imageView3"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.497"
        app:layout_constraintStart_toStartOf="@+id/imageView3"
        app:layout_constraintTop_toBottomOf="@+id/editTextText"
        app:layout_constraintVertical_bias="0.118" />

    <ImageView
        android:id="@+id/imageView4"
        android:layout_width="39dp"
        android:layout_height="39dp"
        app:layout_constraintEnd_toStartOf="@+id/textView4"
        app:layout_constraintHorizontal_bias="1.0"
        app:layout_constraintStart_toStartOf="parent"
        app:srcCompat="@android:drawable/ic_menu_share"
        tools:layout_editor_absoluteY="634dp" />

    <TextView
        android:id="@+id/textView4"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Share profile"
        android:textColor="#000000"
        android:textSize="16sp"
        app:layout_constraintBottom_toBottomOf="@+id/imageView3"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.27"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/editTextDate"
        app:layout_constraintVertical_bias="0.788" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

#### Адаптация под альбомную ориентацию
Для корректной работы приложения при повороте устройства был создан альтернативный файл разметки `activity_second.xml (land)`. В нем расположение элементов оптимизировано для горизонтального пространства. Переключение на данный экран программно зафиксировано в коде `MainActivity.java`.

### 3.3. Программирование логики (модуль `ButtonClicker`)
На данном этапе была реализована связь между XML-разметкой и Java-кодом. Основная задача — управление состоянием `TextView` и `CheckBox` при нажатии на кнопки.

В работе применены два способа обработки событий нажатия:

1.  **С помощью анонимного класса (слушателя):** Для кнопки `btnWhoAmI` программно назначен `OnClickListener`. При клике в текстовое поле записывается информация о номере студента, а флажок `CheckBox` устанавливается в активное положение.
2.  **С помощью атрибута в XML:** Для кнопки `btnItIsNotMe` использован метод `onMyButtonClick`, прописанный непосредственно в свойствах кнопки в XML. Этот метод сбрасывает состояние флажка и выводит уведомление типа `Toast`.

**Класс `MainActivity.java`:**
```java
package ru.mirea.danilovma.buttonclicker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView textViewStudent;
    private Button btnWhoAmI;
    private Button btnItIsNotMe;
    private CheckBox checkBox;

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

        textViewStudent = findViewById(R.id.tvOut);
        btnWhoAmI = findViewById(R.id.btnWhoAmI);
        btnItIsNotMe = findViewById(R.id.btnItIsNotMe);
        checkBox = findViewById(R.id.checkBox);

        View.OnClickListener oclBtnWhoAmI = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewStudent.setText("Мой номер по списку № 5");
                checkBox.setChecked(true);
            }
        };

        btnWhoAmI.setOnClickListener(oclBtnWhoAmI);
    }

    public void onMyButtonClick(View view) {
        textViewStudent.setText("Это не я сделал");
        checkBox.setChecked(false);
        Toast.makeText(this, "Ещё один способ!", Toast.LENGTH_SHORT).show();
    }
}
```

---

## 4. Итоги практической работы
По завершении первой практической работы были достигнуты следующие результаты:
*   Изучены принципы построения интерфейсов Android-приложений с использованием различных типов контейнеров.
*   Освоена работа с системой ресурсов (строки, изображения, разметки) и адаптация UI под разные режимы экрана.
*   Получен опыт написания обработчиков событий на Java, изучены методы поиска View-элементов в иерархии и программного изменения их атрибутов.
*   Приложение успешно протестировано на эмуляторе, весь функционал работает корректно.