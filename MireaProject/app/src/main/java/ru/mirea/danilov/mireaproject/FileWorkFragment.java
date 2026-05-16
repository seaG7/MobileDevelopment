package ru.mirea.danilov.mireaproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ru.mirea.danilov.mireaproject.databinding.DialogFileRecordBinding;
import ru.mirea.danilov.mireaproject.databinding.FragmentFileWorkBinding;

public class FileWorkFragment extends Fragment {

    private static final String PREFS_NAME = "file_work_preferences";
    private static final String KEY_AES_KEY = "aes_key";

    private static final String DIRECTORY_NAME = "encrypted_notes";
    private static final String FILE_EXTENSION = ".secure.txt";

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int GCM_TAG_SIZE = 128;

    private FragmentFileWorkBinding binding;
    private File notesDirectory;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss",
            Locale.getDefault()
    );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFileWorkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        notesDirectory = new File(requireContext().getFilesDir(), DIRECTORY_NAME);

        if (!notesDirectory.exists() && !notesDirectory.mkdirs()) {
            binding.statusTextView.setText("Не удалось создать директорию для файлов.");
        }

        binding.addFileFab.setOnClickListener(v -> showCreateRecordDialog());

        binding.refreshFilesButton.setOnClickListener(v -> loadFiles());

        binding.clearFilesButton.setOnClickListener(v -> confirmClearFiles());

        loadFiles();
    }

    private void showCreateRecordDialog() {
        DialogFileRecordBinding dialogBinding = DialogFileRecordBinding.inflate(
                getLayoutInflater()
        );

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Новая защищённая запись")
                .setView(dialogBinding.getRoot())
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(
                DialogInterface.BUTTON_POSITIVE
        ).setOnClickListener(v -> {
            String title = dialogBinding.editTextRecordTitle.getText().toString().trim();
            String text = dialogBinding.editTextRecordText.getText().toString().trim();

            if (title.isEmpty() || text.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Введите название и текст записи",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            try {
                createEncryptedFile(title, text);
                dialog.dismiss();
                loadFiles();

                Toast.makeText(
                        requireContext(),
                        "Запись сохранена в файл",
                        Toast.LENGTH_SHORT
                ).show();

            } catch (Exception exception) {
                Toast.makeText(
                        requireContext(),
                        "Ошибка сохранения: " + exception.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }));

        dialog.show();
    }

    private void createEncryptedFile(String title, String text) throws Exception {
        if (!notesDirectory.exists() && !notesDirectory.mkdirs()) {
            throw new IOException("Не удалось создать директорию");
        }

        String safeTitle = makeSafeFileName(title);
        String fileName = System.currentTimeMillis() + "_" + safeTitle + FILE_EXTENSION;

        File file = new File(notesDirectory, fileName);

        String encryptedText = encryptText(text);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file, false),
                StandardCharsets.UTF_8
        )) {
            writer.write(title);
            writer.write("\n");
            writer.write(encryptedText);
        }
    }

    private void loadFiles() {
        binding.filesContainer.removeAllViews();

        if (!notesDirectory.exists() && !notesDirectory.mkdirs()) {
            binding.statusTextView.setText("Директория с файлами недоступна.");
            binding.emptyFilesTextView.setVisibility(View.VISIBLE);
            return;
        }

        File[] files = notesDirectory.listFiles(
                (directory, name) -> name.endsWith(FILE_EXTENSION)
        );

        if (files == null || files.length == 0) {
            binding.emptyFilesTextView.setVisibility(View.VISIBLE);
            binding.statusTextView.setText(
                    "Защищённых записей пока нет.\nНажмите кнопку + для создания файла."
            );
            return;
        }

        Arrays.sort(files, (firstFile, secondFile) ->
                Long.compare(secondFile.lastModified(), firstFile.lastModified())
        );

        binding.emptyFilesTextView.setVisibility(View.GONE);

        for (File file : files) {
            addFileView(file);
        }

        binding.statusTextView.setText(
                "Найдено файлов: " + files.length + "\n"
                        + "Каталог: " + notesDirectory.getAbsolutePath()
        );
    }

    private void addFileView(File file) {
        TextView fileTextView = new TextView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);

        fileTextView.setLayoutParams(params);
        fileTextView.setTextSize(16);
        fileTextView.setPadding(16, 16, 16, 16);

        try {
            Record record = readRecordFromFile(file);

            String text = "Название: " + record.title + "\n"
                    + "Файл: " + file.getName() + "\n"
                    + "Размер: " + file.length() + " байт\n"
                    + "Изменён: " + dateFormat.format(new Date(file.lastModified())) + "\n"
                    + "Нажмите, чтобы открыть и расшифровать";

            fileTextView.setText(text);

        } catch (Exception exception) {
            fileTextView.setText(
                    "Файл: " + file.getName() + "\n"
                            + "Ошибка чтения или расшифровки\n"
                            + "Нажмите, чтобы попробовать открыть"
            );
        }

        fileTextView.setOnClickListener(v -> showFileDialog(file));

        binding.filesContainer.addView(fileTextView);
    }

    private void showFileDialog(File file) {
        try {
            Record record = readRecordFromFile(file);

            String message = "Файл: " + file.getName() + "\n"
                    + "Размер: " + file.length() + " байт\n"
                    + "Изменён: " + dateFormat.format(new Date(file.lastModified())) + "\n\n"
                    + "Расшифрованный текст:\n"
                    + record.text;

            new AlertDialog.Builder(requireContext())
                    .setTitle(record.title)
                    .setMessage(message)
                    .setNegativeButton("Удалить", (dialogInterface, which) -> {
                        if (file.delete()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Файл удалён",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Не удалось удалить файл",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        loadFiles();
                    })
                    .setPositiveButton("Закрыть", null)
                    .show();

        } catch (Exception exception) {
            Toast.makeText(
                    requireContext(),
                    "Ошибка открытия файла: " + exception.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private Record readRecordFromFile(File file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file),
                        StandardCharsets.UTF_8
                )
        )) {
            String title = reader.readLine();
            String iv = reader.readLine();
            String encryptedText = reader.readLine();

            if (title == null || iv == null || encryptedText == null) {
                throw new IOException("Файл имеет неверный формат");
            }

            String decryptedText = decryptText(iv + "\n" + encryptedText);

            return new Record(title, decryptedText);
        }
    }

    private void confirmClearFiles() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление файлов")
                .setMessage("Удалить все защищённые записи?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialogInterface, which) -> clearAllFiles())
                .show();
    }

    private void clearAllFiles() {
        File[] files = notesDirectory.listFiles(
                (directory, name) -> name.endsWith(FILE_EXTENSION)
        );

        int deletedCount = 0;

        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    deletedCount++;
                }
            }
        }

        loadFiles();

        Toast.makeText(
                requireContext(),
                "Удалено файлов: " + deletedCount,
                Toast.LENGTH_SHORT
        ).show();
    }

    private String encryptText(String text) throws Exception {
        SecretKey secretKey = getOrCreateSecretKey();

        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(
                GCM_TAG_SIZE,
                iv
        );

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(
                text.getBytes(StandardCharsets.UTF_8)
        );

        String encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP);
        String encodedEncryptedText = Base64.encodeToString(
                encryptedBytes,
                Base64.NO_WRAP
        );

        return encodedIv + "\n" + encodedEncryptedText;
    }

    private String decryptText(String encryptedData) throws Exception {
        String[] parts = encryptedData.split("\n", 2);

        if (parts.length != 2) {
            throw new IOException("Неверный формат зашифрованных данных");
        }

        byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP);

        SecretKey secretKey = getOrCreateSecretKey();

        Cipher cipher = Cipher.getInstance(AES_MODE);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(
                GCM_TAG_SIZE,
                iv
        );

        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private SecretKey getOrCreateSecretKey() throws Exception {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        String encodedKey = sharedPreferences.getString(KEY_AES_KEY, null);

        if (encodedKey == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE);

            SecretKey secretKey = keyGenerator.generateKey();

            String newEncodedKey = Base64.encodeToString(
                    secretKey.getEncoded(),
                    Base64.NO_WRAP
            );

            sharedPreferences.edit()
                    .putString(KEY_AES_KEY, newEncodedKey)
                    .apply();

            return secretKey;
        }

        byte[] decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP);

        return new SecretKeySpec(decodedKey, AES_ALGORITHM);
    }

    private String makeSafeFileName(String title) {
        String result = title
                .trim()
                .replaceAll("[^a-zA-Zа-яА-Я0-9_-]+", "_");

        if (result.isEmpty()) {
            result = "note";
        }

        if (result.length() > 32) {
            result = result.substring(0, 32);
        }

        return result;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class Record {

        private final String title;
        private final String text;

        private Record(String title, String text) {
            this.title = title;
            this.text = text;
        }
    }
}