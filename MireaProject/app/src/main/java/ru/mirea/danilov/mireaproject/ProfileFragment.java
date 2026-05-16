package ru.mirea.danilov.mireaproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ru.mirea.danilov.mireaproject.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private static final String PREFS_NAME = "profile_preferences";

    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_GROUP = "group";
    private static final String KEY_LIST_NUMBER = "list_number";
    private static final String KEY_DIRECTION = "direction";
    private static final String KEY_GOAL = "goal";
    private static final String KEY_RECOMMENDATIONS = "recommendations";

    private FragmentProfileBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        loadProfile();

        binding.buttonSaveProfile.setOnClickListener(v -> saveProfile());

        binding.buttonClearProfile.setOnClickListener(v -> clearProfile());
    }

    private void saveProfile() {
        String fullName = binding.editTextFullName.getText().toString().trim();
        String group = binding.editTextGroup.getText().toString().trim();
        String listNumberText = binding.editTextListNumber.getText().toString().trim();
        String direction = binding.editTextDirection.getText().toString().trim();
        String goal = binding.editTextGoal.getText().toString().trim();
        boolean recommendations = binding.checkBoxRecommendations.isChecked();

        if (fullName.isEmpty()
                || group.isEmpty()
                || listNumberText.isEmpty()
                || direction.isEmpty()
                || goal.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Заполните все поля профиля",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int listNumber;

        try {
            listNumber = Integer.parseInt(listNumberText);
        } catch (NumberFormatException exception) {
            Toast.makeText(
                    requireContext(),
                    "Номер по списку должен быть числом",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        sharedPreferences.edit()
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_GROUP, group)
                .putInt(KEY_LIST_NUMBER, listNumber)
                .putString(KEY_DIRECTION, direction)
                .putString(KEY_GOAL, goal)
                .putBoolean(KEY_RECOMMENDATIONS, recommendations)
                .apply();

        showProfileSummary();

        Toast.makeText(
                requireContext(),
                "Профиль сохранён",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void loadProfile() {
        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");
        String group = sharedPreferences.getString(KEY_GROUP, "");
        int listNumber = sharedPreferences.getInt(KEY_LIST_NUMBER, -1);
        String direction = sharedPreferences.getString(KEY_DIRECTION, "");
        String goal = sharedPreferences.getString(KEY_GOAL, "");
        boolean recommendations = sharedPreferences.getBoolean(KEY_RECOMMENDATIONS, false);

        binding.editTextFullName.setText(fullName);
        binding.editTextGroup.setText(group);

        if (listNumber == -1) {
            binding.editTextListNumber.setText("");
        } else {
            binding.editTextListNumber.setText(String.valueOf(listNumber));
        }

        binding.editTextDirection.setText(direction);
        binding.editTextGoal.setText(goal);
        binding.checkBoxRecommendations.setChecked(recommendations);

        showProfileSummary();
    }

    private void showProfileSummary() {
        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");
        String group = sharedPreferences.getString(KEY_GROUP, "");
        int listNumber = sharedPreferences.getInt(KEY_LIST_NUMBER, -1);
        String direction = sharedPreferences.getString(KEY_DIRECTION, "");
        String goal = sharedPreferences.getString(KEY_GOAL, "");
        boolean recommendations = sharedPreferences.getBoolean(KEY_RECOMMENDATIONS, false);

        if (fullName.isEmpty()
                && group.isEmpty()
                && listNumber == -1
                && direction.isEmpty()
                && goal.isEmpty()) {

            binding.textViewProfileResult.setText(
                    "Профиль пока не заполнен. Введите данные и нажмите кнопку сохранения."
            );

            return;
        }

        String result = "Сохранённый профиль:\n\n"
                + "ФИО: " + fullName + "\n"
                + "Группа: " + group + "\n"
                + "Номер по списку: " + listNumber + "\n"
                + "Направление интересов: " + direction + "\n"
                + "Цель использования приложения: " + goal + "\n"
                + "Рекомендации включены: " + (recommendations ? "да" : "нет");

        binding.textViewProfileResult.setText(result);
    }

    private void clearProfile() {
        sharedPreferences.edit().clear().apply();

        binding.editTextFullName.setText("");
        binding.editTextGroup.setText("");
        binding.editTextListNumber.setText("");
        binding.editTextDirection.setText("");
        binding.editTextGoal.setText("");
        binding.checkBoxRecommendations.setChecked(false);

        binding.textViewProfileResult.setText(
                "Профиль очищен. Данные удалены из SharedPreferences."
        );

        Toast.makeText(
                requireContext(),
                "Профиль очищен",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}