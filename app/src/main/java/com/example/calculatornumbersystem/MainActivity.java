package com.example.calculatornumbersystem;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextView; // Поле для отображения результата
    private EditText fromBaseEditText, toBaseEditText; // Поля для ввода систем счисления
    private String currentInput = ""; // Текущий ввод пользователя

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация полей для ввода систем счисления
        fromBaseEditText = findViewById(R.id.fromBaseEditText);
        toBaseEditText = findViewById(R.id.toBaseEditText);

        // Инициализация TextView для отображения результата
        resultTextView = findViewById(R.id.resultTextView);

        // Цифры
        setDigitButtonListener(R.id.button0, "0");
        setDigitButtonListener(R.id.button1, "1");
        setDigitButtonListener(R.id.button2, "2");
        setDigitButtonListener(R.id.button3, "3");
        setDigitButtonListener(R.id.button4, "4");
        setDigitButtonListener(R.id.button5, "5");
        setDigitButtonListener(R.id.button6, "6");
        setDigitButtonListener(R.id.button7, "7");
        setDigitButtonListener(R.id.button8, "8");
        setDigitButtonListener(R.id.button9, "9");

        // Кнопка точки (.)
        ImageButton buttonDot = findViewById(R.id.buttonDot);
        buttonDot.setOnClickListener(v -> {
            if (!currentInput.contains(".")) { // Добавляем точку только один раз
                currentInput += ".";
                resultTextView.setText(currentInput);
            }
        });

        // Кнопка очистки (AC)
        ImageButton buttonAC = findViewById(R.id.buttonAC);
        buttonAC.setOnClickListener(v -> clearInput());

        // Кнопка вычисления (=)
        ImageButton buttonEquals = findViewById(R.id.buttonEquals);
        buttonEquals.setOnClickListener(v -> convertNumber());
    }

    // Метод для добавления цифр
    private void setDigitButtonListener(int buttonId, String digit) {
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener(v -> appendToInput(digit));
    }

    // Метод для добавления символов в поле ввода
    private void appendToInput(String value) {
        currentInput += value;
        resultTextView.setText(currentInput);
    }

    // Метод для очистки ввода
    private void clearInput() {
        currentInput = "";
        resultTextView.setText("0");
    }

    // Метод для перевода числа между системами счисления
    private void convertNumber() {
        try {
            int fromBase = Integer.parseInt(fromBaseEditText.getText().toString());
            int toBase = Integer.parseInt(toBaseEditText.getText().toString());
            String numberStr = resultTextView.getText().toString();

            if (TextUtils.isEmpty(numberStr) || fromBase <= 0 || toBase <= 0) {
                resultTextView.setText("Ошибка ввода");
                return;
            }

            // Преобразуем число из исходной системы в десятичную
            long decimalNumber = Long.parseLong(numberStr, fromBase);

            // Преобразуем десятичное число в целевую систему счисления
            String convertedNumber = Long.toString(decimalNumber, toBase);

            // Отображаем результат
            resultTextView.setText(convertedNumber);
        } catch (NumberFormatException e) {
            resultTextView.setText("Ошибка ввода");
        }
    }
}