package com.example.calculatornumbersystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import com.example.calculatornumbersystem.databinding.ActivityNumberSystemBinding;
import com.google.android.material.button.MaterialButton;

public class NumberSystemActivity extends AppCompatActivity {

    private ActivityNumberSystemBinding binding;
    private String currentInput = "0";
    private boolean newInput = true;

    private static final String PREFS_NAME = "CalculatorPrefs";
    private static final String KEY_FROM_BASE = "fromBase";
    private static final String KEY_TO_BASE = "toBase";
    private static final String KEY_NUM_SYS_INPUT = "numSysInput";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNumberSystemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Восстановление состояния
        if (savedInstanceState != null) {
            currentInput = savedInstanceState.getString(KEY_NUM_SYS_INPUT, "0");
            binding.fromBaseEditText.setText(savedInstanceState.getString(KEY_FROM_BASE, "10"));
            binding.toBaseEditText.setText(savedInstanceState.getString(KEY_TO_BASE, "2"));
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentInput = prefs.getString(KEY_NUM_SYS_INPUT, "0");
            binding.fromBaseEditText.setText(prefs.getString(KEY_FROM_BASE, "10"));
            binding.toBaseEditText.setText(prefs.getString(KEY_TO_BASE, "2"));
        }

        updateDisplay();
        setupButtons();

        binding.btnSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(NumberSystemActivity.this, StandardCalculatorActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupButtons() {
        // Цифровые кнопки (0-9)
        int[] numberButtons = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int buttonId : numberButtons) {
            MaterialButton button = findViewById(buttonId);
            button.setOnClickListener(v -> {
                String digit = button.getText().toString();
                appendDigit(digit);
            });
        }

        // Кнопки букв (A-F)
        int[] letterButtons = {R.id.btn_A, R.id.btn_B, R.id.btn_C, R.id.btn_D, R.id.btn_E, R.id.btn_F};
        for (int buttonId : letterButtons) {
            MaterialButton button = findViewById(buttonId);
            button.setOnClickListener(v -> {
                String letter = button.getText().toString();
                appendDigit(letter);
            });
        }

        // Кнопка очистки
        binding.btnAc.setOnClickListener(v -> {
            currentInput = "0";
            newInput = true;
            updateDisplay();
        });

        // Кнопка удаления
        binding.btnBackspace.setOnClickListener(v -> {
            if (currentInput.length() > 1) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            } else {
                currentInput = "0";
                newInput = true;
            }
            updateDisplay();
        });

        // Кнопка равно
        binding.btnEquals.setOnClickListener(v -> convertNumber());

        // Остальные кнопки (не поддерживаются)
        int[] unsupportedButtons = {R.id.btn_plus, R.id.btn_minus, R.id.btn_multiply,
                R.id.btn_divide, R.id.btn_percent, R.id.btn_dot};
        for (int buttonId : unsupportedButtons) {
            findViewById(buttonId).setOnClickListener(v ->
                    binding.resultTextView.setText(getString(R.string.not_supported)));
        }
    }

    private void appendDigit(String digit) {
        if (newInput || currentInput.equals("0")) {
            currentInput = digit;
            newInput = false;
        } else {
            currentInput += digit;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        binding.resultTextView.setText(currentInput);
    }

    private void convertNumber() {
        String fromBaseStr = binding.fromBaseEditText.getText().toString();
        String toBaseStr = binding.toBaseEditText.getText().toString();

        if (TextUtils.isEmpty(fromBaseStr) || TextUtils.isEmpty(toBaseStr)) {
            binding.resultTextView.setText(getString(R.string.error_missing_base));
            return;
        }

        try {
            int fromBase = Integer.parseInt(fromBaseStr);
            int toBase = Integer.parseInt(toBaseStr);

            if (fromBase < 2 || fromBase > 36 || toBase < 2 || toBase > 36) {
                binding.resultTextView.setText(getString(R.string.error_invalid_base));
                return;
            }

            if (currentInput.isEmpty() || currentInput.equals("0")) {
                binding.resultTextView.setText("0");
                return;
            }

            // Проверка символов на соответствие исходной системе счисления
            for (char c : currentInput.toUpperCase().toCharArray()) {
                int digitValue = Character.digit(c, fromBase);
                if (digitValue == -1) {
                    binding.resultTextView.setText(getString(R.string.error_invalid_input));
                    return;
                }
            }

            long decimalValue = Long.parseLong(currentInput, fromBase);
            String convertedNumber = Long.toString(decimalValue, toBase).toUpperCase();
            currentInput = convertedNumber;
            newInput = true;
            updateDisplay();
        } catch (NumberFormatException e) {
            binding.resultTextView.setText(getString(R.string.error_invalid_input));
        } catch (Exception e) {
            binding.resultTextView.setText(getString(R.string.error_generic));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_FROM_BASE, binding.fromBaseEditText.getText().toString());
        editor.putString(KEY_TO_BASE, binding.toBaseEditText.getText().toString());
        editor.putString(KEY_NUM_SYS_INPUT, currentInput);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NUM_SYS_INPUT, currentInput);
        outState.putString(KEY_FROM_BASE, binding.fromBaseEditText.getText().toString());
        outState.putString(KEY_TO_BASE, binding.toBaseEditText.getText().toString());
    }
}