package ru.mirea.danilov.mireaproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;

import ru.mirea.danilov.mireaproject.databinding.FragmentMicrophoneBinding;

public class MicrophoneFragment extends Fragment {

    private final String TAG = MicrophoneFragment.class.getSimpleName();

    private FragmentMicrophoneBinding binding;

    private String recordFilePath;
    private MediaRecorder recorder;
    private MediaPlayer player;

    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean hasRecord = false;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startRecording();
                        } else {
                            Toast.makeText(requireContext(), "Разрешение на микрофон не получено", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentMicrophoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        recordFilePath = new File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "voice_note.3gp"
        ).getAbsolutePath();

        binding.playButton.setEnabled(false);

        binding.recordButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                checkAudioPermission();
            }
        });

        binding.playButton.setOnClickListener(v -> {
            if (isPlaying) {
                stopPlaying();
            } else {
                startPlaying();
            }
        });
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();

            isRecording = true;

            binding.recordButton.setText("Остановить запись");
            binding.playButton.setEnabled(false);

            String title = binding.titleEditText.getText().toString();

            if (title.isEmpty()) {
                binding.statusTextView.setText("Идет запись голосовой заметки...");
            } else {
                binding.statusTextView.setText("Идет запись голосовой заметки: " + title);
            }

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            Toast.makeText(requireContext(), "Ошибка записи", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        isRecording = false;
        hasRecord = true;

        binding.recordButton.setText("Начать запись");
        binding.playButton.setEnabled(true);
        binding.statusTextView.setText("Голосовая заметка сохранена.");
    }

    private void startPlaying() {
        if (!hasRecord) {
            Toast.makeText(requireContext(), "Сначала запишите голосовую заметку", Toast.LENGTH_SHORT).show();
            return;
        }

        player = new MediaPlayer();

        try {
            player.setDataSource(recordFilePath);
            player.prepare();
            player.start();

            isPlaying = true;

            binding.playButton.setText("Остановить воспроизведение");
            binding.recordButton.setEnabled(false);
            binding.statusTextView.setText("Воспроизведение голосовой заметки...");

            player.setOnCompletionListener(mediaPlayer -> stopPlaying());

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            Toast.makeText(requireContext(), "Ошибка воспроизведения", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }

        isPlaying = false;

        binding.playButton.setText("Воспроизвести");
        binding.recordButton.setEnabled(true);
        binding.statusTextView.setText("Воспроизведение остановлено.");
    }

    @Override
    public void onStop() {
        super.onStop();

        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}