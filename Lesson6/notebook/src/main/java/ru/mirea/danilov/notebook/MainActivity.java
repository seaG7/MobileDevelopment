package ru.mirea.danilov.notebook;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String MIME_TYPE_TEXT = "text/plain";
    private static final String RELATIVE_DOCUMENTS_PATH = Environment.DIRECTORY_DOCUMENTS + "/";

    private EditText editTextFileName;
    private EditText editTextQuote;
    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFileName = findViewById(R.id.editTextFileName);
        editTextQuote = findViewById(R.id.editTextQuote);
        textViewStatus = findViewById(R.id.textViewStatus);

        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonLoad = findViewById(R.id.buttonLoad);

        editTextFileName.setText("quote_pushkin.txt");
        editTextQuote.setText("Гений и злодейство — две вещи несовместные.");

        requestStoragePermissionForOldAndroid();

        buttonSave.setOnClickListener(view -> saveQuote());
        buttonLoad.setOnClickListener(view -> loadQuote());
    }

    private void saveQuote() {
        String fileName = prepareFileName(editTextFileName.getText().toString());
        String quote = editTextQuote.getText().toString();

        if (fileName.isEmpty()) {
            Toast.makeText(this, "Введите название файла", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quote.trim().isEmpty()) {
            Toast.makeText(this, "Введите цитату", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasStorageAccess()) {
            requestStoragePermissionForOldAndroid();
            Toast.makeText(this, "Нужно разрешение на запись", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            saveToDocuments(fileName, quote);
            textViewStatus.setText("Файл сохранён: Documents/" + fileName);
            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            textViewStatus.setText("Ошибка записи: " + exception.getMessage());
            Toast.makeText(this, "Ошибка записи", Toast.LENGTH_LONG).show();
        }
    }

    private void loadQuote() {
        String fileName = prepareFileName(editTextFileName.getText().toString());

        if (fileName.isEmpty()) {
            Toast.makeText(this, "Введите название файла", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String text = readFromDocuments(fileName);
            editTextQuote.setText(text);
            textViewStatus.setText("Файл загружен: Documents/" + fileName);
            Toast.makeText(this, "Данные загружены", Toast.LENGTH_SHORT).show();
        } catch (IOException exception) {
            textViewStatus.setText("Ошибка чтения: " + exception.getMessage());
            Toast.makeText(this, "Ошибка чтения", Toast.LENGTH_LONG).show();
        }
    }

    private String prepareFileName(String fileName) {
        String result = fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

        if (result.isEmpty()) {
            return "";
        }

        if (!result.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            result = result + ".txt";
        }

        return result;
    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermissionForOldAndroid() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION
            );
        }
    }

    private void saveToDocuments(String fileName, String text) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDocumentsUsingMediaStore(fileName, text);
        } else {
            saveToDocumentsUsingFileApi(fileName, text);
        }
    }

    private String readFromDocuments(String fileName) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return readFromDocumentsUsingMediaStore(fileName);
        } else {
            return readFromDocumentsUsingFileApi(fileName);
        }
    }

    private void saveToDocumentsUsingMediaStore(String fileName, String text) throws IOException {
        ContentResolver resolver = getContentResolver();
        Uri collectionUri = MediaStore.Files.getContentUri("external");

        Uri fileUri = findFileUri(fileName);

        if (fileUri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE_TEXT);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_DOCUMENTS_PATH);

            fileUri = resolver.insert(collectionUri, values);

            if (fileUri == null) {
                throw new IOException("Не удалось создать файл");
            }
        }

        try (OutputStream outputStream = resolver.openOutputStream(fileUri, "wt")) {
            if (outputStream == null) {
                throw new IOException("Не удалось открыть поток записи");
            }

            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readFromDocumentsUsingMediaStore(String fileName) throws IOException {
        Uri fileUri = findFileUri(fileName);

        if (fileUri == null) {
            throw new IOException("Файл не найден");
        }

        try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null) {
                throw new IOException("Не удалось открыть поток чтения");
            }

            return readAllText(inputStream);
        }
    }

    private Uri findFileUri(String fileName) {
        Uri collectionUri = MediaStore.Files.getContentUri("external");

        String[] projection = new String[]{
                MediaStore.MediaColumns._ID
        };

        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND "
                + MediaStore.MediaColumns.RELATIVE_PATH + "=?";

        String[] selectionArgs = new String[]{
                fileName,
                RELATIVE_DOCUMENTS_PATH
        };

        try (Cursor cursor = getContentResolver().query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                return ContentUris.withAppendedId(collectionUri, id);
            }
        }

        return null;
    }

    private void saveToDocumentsUsingFileApi(String fileName, String text) throws IOException {
        if (!isExternalStorageWritable()) {
            throw new IOException("Внешнее хранилище недоступно для записи");
        }

        File documentsDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
        );

        if (!documentsDirectory.exists() && !documentsDirectory.mkdirs()) {
            throw new IOException("Не удалось создать папку Documents");
        }

        File file = new File(documentsDirectory, fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file, false);
             OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

            writer.write(text);
        }
    }

    private String readFromDocumentsUsingFileApi(String fileName) throws IOException {
        File documentsDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
        );

        File file = new File(documentsDirectory, fileName);

        if (!file.exists()) {
            throw new IOException("Файл не найден");
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return readAllText(fileInputStream);
        }
    }

    private String readAllText(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )) {
            String line = reader.readLine();

            while (line != null) {
                if (result.length() > 0) {
                    result.append('\n');
                }

                result.append(line);
                line = reader.readLine();
            }
        }

        return result.toString();
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}