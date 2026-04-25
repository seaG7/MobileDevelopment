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