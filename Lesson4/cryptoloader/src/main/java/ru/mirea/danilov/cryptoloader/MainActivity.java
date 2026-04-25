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