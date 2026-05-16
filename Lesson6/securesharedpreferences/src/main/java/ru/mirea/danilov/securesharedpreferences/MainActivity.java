package ru.mirea.danilov.securesharedpreferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private static final String SECRET_PREFS_NAME = "secret_shared_prefs";
    private static final String KEY_SECURE = "secure";
    private static final String FAVORITE_POET = "Александр Сергеевич Пушкин";

    private TextView textViewPoetName;
    private TextView textViewSecureResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewPoetName = findViewById(R.id.textViewPoetName);
        textViewSecureResult = findViewById(R.id.textViewSecureResult);

        saveAndLoadEncryptedData();
    }

    private void saveAndLoadEncryptedData() {
        try {
            KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
            String mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);

            SharedPreferences secureSharedPreferences = EncryptedSharedPreferences.create(
                    SECRET_PREFS_NAME,
                    mainKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            secureSharedPreferences.edit()
                    .putString(KEY_SECURE, FAVORITE_POET)
                    .apply();

            String result = secureSharedPreferences.getString(KEY_SECURE, "Нет данных");

            textViewPoetName.setText(result);
            textViewSecureResult.setText("Значение получено из EncryptedSharedPreferences");

        } catch (GeneralSecurityException | IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}