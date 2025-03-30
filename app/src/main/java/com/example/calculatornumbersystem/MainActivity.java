package com.example.calculatornumbersystem;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    // Для калькулятора систем счисления
    private EditText fromBaseEditText, toBaseEditText;
    private TextView resultTextView;
    private String currentInput = "0";

    // Для обычного калькулятора
    private TextView standardResultTextView;
    private String currentNumber = "";
    private String previousNumber = "";
    private String operation = "";
    private boolean isNewCalculation = true;

    // Режим работы
    private boolean isNumberSystemMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadNumberSystemCalculator();
    }

    private void loadNumberSystemCalculator() {
        setContentView(R.layout.activity_main);
        isNumberSystemMode = true;

        // Инициализация элементов
        fromBaseEditText = findViewById(R.id.fromBaseEditText);
        toBaseEditText = findViewById(R.id.toBaseEditText);
        resultTextView = findViewById(R.id.resultTextView);

        // Настройка кнопок
        setupNumberSystemButtons();

        // Кнопка переключения
        MaterialButton switchBtn = findViewById(R.id.btn_switch);
        switchBtn.setOnClickListener(v -> {
            Log.d("CALCULATOR", "Switching to Standard Calculator");
            loadStandardCalculator();
        });
        switchBtn.setVisibility(View.VISIBLE);
    }

    private void setupNumberSystemButtons() {
        // Цифры 0-9
        int[] numberButtons = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int i = 0; i < numberButtons.length; i++) {
            final String number = String.valueOf(i);
            findViewById(numberButtons[i]).setOnClickListener(v -> appendNumberSystemInput(number));
        }

        // Точка
        findViewById(R.id.btn_dot).setOnClickListener(v -> {
            if (!currentInput.contains(".")) {
                appendNumberSystemInput(".");
            }
        });

        // Очистка
        findViewById(R.id.btn_ac).setOnClickListener(v -> clearNumberSystemInput());

        // Конвертация
        findViewById(R.id.btn_equals).setOnClickListener(v -> convertNumber());
    }

    private void appendNumberSystemInput(String value) {
        if (currentInput.equals("0") && !value.equals(".")) {
            currentInput = value;
        } else {
            currentInput += value;
        }
        resultTextView.setText(currentInput);
    }

    private void clearNumberSystemInput() {
        currentInput = "0";
        resultTextView.setText(currentInput);
    }

    private void convertNumber() {
        try {
            int fromBase = getValidBase(fromBaseEditText);
            int toBase = getValidBase(toBaseEditText);

            if (fromBase < 2 || toBase < 2 || fromBase > 36 || toBase > 36) {
                showError("Основание 2-36");
                return;
            }

            String numberStr = currentInput.replace(".", "");
            long decimalNumber = Long.parseLong(numberStr, fromBase);
            String convertedNumber = Long.toString(decimalNumber, toBase).toUpperCase();

            resultTextView.setText(convertedNumber);
            currentInput = convertedNumber;

        } catch (NumberFormatException e) {
            showError("Некорректный ввод");
        }
    }

    private int getValidBase(EditText editText) {
        String text = editText.getText().toString();
        if (TextUtils.isEmpty(text)) return 0;
        return Integer.parseInt(text);
    }

    private void showError(String message) {
        resultTextView.setText(message);
        currentInput = "0";
    }

    private void loadStandardCalculator() {
        setContentView(R.layout.activity_standard_calculator);
        isNumberSystemMode = false;

        // Инициализация элементов
        standardResultTextView = findViewById(R.id.standardResultTextView);

        // Настройка кнопок
        setupStandardCalculatorButtons();

        // Кнопка переключения
        MaterialButton switchBtn = findViewById(R.id.btn_switch);
        switchBtn.setOnClickListener(v -> {
            Log.d("CALCULATOR", "Switching to Number System Calculator");
            loadNumberSystemCalculator();
        });
        switchBtn.setVisibility(View.VISIBLE);
    }

    private void setupStandardCalculatorButtons() {
        // Цифры 0-9
        int[] numberButtons = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int buttonId : numberButtons) {
            findViewById(buttonId).setOnClickListener(v -> {
                MaterialButton button = (MaterialButton) v;
                appendStandardInput(button.getText().toString());
            });
        }

        // Операции
        findViewById(R.id.btn_plus).setOnClickListener(v -> setOperation("+"));
        findViewById(R.id.btn_minus).setOnClickListener(v -> setOperation("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> setOperation("×"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> setOperation("/"));
        findViewById(R.id.btn_percent).setOnClickListener(v -> setOperation("%"));

        // Очистка
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearStandardCalculator());

        // Равно
        findViewById(R.id.btn_equals).setOnClickListener(v -> calculateResult());

        // Точка
        findViewById(R.id.btn_dot).setOnClickListener(v -> {
            if (!currentNumber.contains(".")) {
                appendStandardInput(".");
            }
        });

        // Смена знака
        findViewById(R.id.btn_plus_minus).setOnClickListener(v -> toggleSign());
    }

    private void appendStandardInput(String number) {
        if (isNewCalculation) {
            currentNumber = number;
            isNewCalculation = false;
        } else {
            currentNumber += number;
        }
        standardResultTextView.setText(currentNumber);
    }

    private void setOperation(String op) {
        if (!currentNumber.isEmpty()) {
            previousNumber = currentNumber;
            currentNumber = "";
            operation = op;
        }
    }

    private void calculateResult() {
        if (!previousNumber.isEmpty() && !currentNumber.isEmpty() && !operation.isEmpty()) {
            double num1 = Double.parseDouble(previousNumber);
            double num2 = Double.parseDouble(currentNumber);
            double result = 0;

            switch (operation) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "×":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 != 0) {
                        result = num1 / num2;
                    } else {
                        standardResultTextView.setText("Ошибка");
                        return;
                    }
                    break;
                case "%":
                    result = num1 % num2;
                    break;
            }

            currentNumber = String.valueOf(result);
            if (currentNumber.endsWith(".0")) {
                currentNumber = currentNumber.substring(0, currentNumber.length() - 2);
            }

            standardResultTextView.setText(currentNumber);
            previousNumber = "";
            operation = "";
            isNewCalculation = true;
        }
    }

    private void clearStandardCalculator() {
        currentNumber = "";
        previousNumber = "";
        operation = "";
        standardResultTextView.setText("0");
        isNewCalculation = true;
    }

    private void toggleSign() {
        if (!currentNumber.isEmpty()) {
            currentNumber = currentNumber.startsWith("-") ?
                    currentNumber.substring(1) : "-" + currentNumber;
            standardResultTextView.setText(currentNumber);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isNumberSystemMode", isNumberSystemMode);

        if (isNumberSystemMode) {
            outState.putString("currentInput", currentInput);
        } else {
            outState.putString("currentNumber", currentNumber);
            outState.putString("previousNumber", previousNumber);
            outState.putString("operation", operation);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isNumberSystemMode = savedInstanceState.getBoolean("isNumberSystemMode");

        if (isNumberSystemMode) {
            loadNumberSystemCalculator();
            currentInput = savedInstanceState.getString("currentInput", "0");
            resultTextView.setText(currentInput);
        } else {
            loadStandardCalculator();
            currentNumber = savedInstanceState.getString("currentNumber", "");
            previousNumber = savedInstanceState.getString("previousNumber", "");
            operation = savedInstanceState.getString("operation", "");
            standardResultTextView.setText(currentNumber.isEmpty() ? "0" : currentNumber);
        }
    }
}