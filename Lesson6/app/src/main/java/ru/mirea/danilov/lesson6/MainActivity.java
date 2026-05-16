package ru.mirea.danilov.lesson6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "mirea_settings";

    private static final String KEY_GROUP = "GROUP";
    private static final String KEY_NUMBER = "NUMBER";
    private static final String KEY_MOVIE = "MOVIE";

    private EditText editTextGroup;
    private EditText editTextNumber;
    private EditText editTextMovie;
    private TextView textViewStatus;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextGroup = findViewById(R.id.editTextGroup);
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextMovie = findViewById(R.id.editTextMovie);
        textViewStatus = findViewById(R.id.textViewStatus);

        Button buttonSave = findViewById(R.id.buttonSave);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loadData();

        buttonSave.setOnClickListener(view -> saveData());
    }

    private void loadData() {
        String group = sharedPreferences.getString(KEY_GROUP, "");
        int number = sharedPreferences.getInt(KEY_NUMBER, -1);
        String movie = sharedPreferences.getString(KEY_MOVIE, "");

        editTextGroup.setText(group);

        if (number != -1) {
            editTextNumber.setText(String.valueOf(number));
        }

        editTextMovie.setText(movie);

        if (!group.isEmpty() || number != -1 || !movie.isEmpty()) {
            textViewStatus.setText("Данные загружены из SharedPreferences");
        }
    }

    private void saveData() {
        String group = editTextGroup.getText().toString().trim();
        String numberText = editTextNumber.getText().toString().trim();
        String movie = editTextMovie.getText().toString().trim();

        if (group.isEmpty() || numberText.isEmpty() || movie.isEmpty()) {
            Toast.makeText(this, "Заполни все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int number;

        try {
            number = Integer.parseInt(numberText);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Номер по списку должен быть числом", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit()
                .putString(KEY_GROUP, group)
                .putInt(KEY_NUMBER, number)
                .putString(KEY_MOVIE, movie)
                .apply();

        textViewStatus.setText("Сохранено: " + group + ", №" + number + ", " + movie);
        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
    }
}