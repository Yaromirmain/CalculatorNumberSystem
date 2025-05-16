package com.example.calculatornumbersystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.material.button.MaterialButton;

// Import binding classes
import com.example.calculatornumbersystem.databinding.ActivityMainBinding;
import com.example.calculatornumbersystem.databinding.ActivityStandardCalculatorBinding;

// Удалите эти импорты, если они были:
// import javax.script.ScriptEngineManager;
// import javax.script.ScriptEngine;
// import javax.script.ScriptException;

// Добавьте импорты для exp4j
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
// Для более детальной информации об ошибках (опционально)
// import net.objecthunter.exp4j.ValidationResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding bindingNumSys;
    private ActivityStandardCalculatorBinding bindingStdCalc;

    private String currentInputForNumSys = "0";
    private StringBuilder currentExpression = new StringBuilder();
    private boolean isNumberSystemMode = true;

    private static final String PREFS_NAME = "CalculatorPrefs";
    private static final String KEY_FROM_BASE = "fromBase";
    private static final String KEY_TO_BASE = "toBase";
    private static final String KEY_NUM_SYS_INPUT = "numSysInput";
    private static final String KEY_IS_NUM_SYS_MODE = "isNumSysMode";
    private static final String KEY_STD_EXPRESSION = "stdExpression";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            isNumberSystemMode = savedInstanceState.getBoolean(KEY_IS_NUM_SYS_MODE, true);
            if (isNumberSystemMode) {
                currentInputForNumSys = savedInstanceState.getString(KEY_NUM_SYS_INPUT, getString(R.string.initial_result_value));
            } else {
                currentExpression = new StringBuilder(savedInstanceState.getString(KEY_STD_EXPRESSION, ""));
            }
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            isNumberSystemMode = prefs.getBoolean(KEY_IS_NUM_SYS_MODE, true);
            if (isNumberSystemMode) {
                currentInputForNumSys = prefs.getString(KEY_NUM_SYS_INPUT, getString(R.string.initial_result_value));
            } else {
                currentExpression = new StringBuilder(prefs.getString(KEY_STD_EXPRESSION, ""));
            }
        }

        if (isNumberSystemMode) {
            loadNumberSystemCalculatorView();
        } else {
            loadStandardCalculatorView();
        }
    }

    private void updateNumSysDisplay() {
        if (bindingNumSys != null) {
            bindingNumSys.resultTextView.setText(currentInputForNumSys);
        }
    }

    private void updateStandardDisplay() {
        if (bindingStdCalc != null) {
            if (currentExpression.length() == 0) {
                bindingStdCalc.standardResultTextView.setText(getString(R.string.initial_result_value));
            } else {
                bindingStdCalc.standardResultTextView.setText(currentExpression.toString());
            }
        }
    }

    private void loadNumberSystemCalculatorView() {
        bindingNumSys = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingNumSys.getRoot());
        bindingStdCalc = null;
        isNumberSystemMode = true;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bindingNumSys.fromBaseEditText.setText(prefs.getString(KEY_FROM_BASE, getString(R.string.default_from_base)));
        bindingNumSys.toBaseEditText.setText(prefs.getString(KEY_TO_BASE, getString(R.string.default_to_base)));
        updateNumSysDisplay();

        setupNumberSystemButtons();
        bindingNumSys.btnSwitch.setOnClickListener(v -> loadStandardCalculatorView());
    }

    private void setupNumberSystemButtons() {
        MaterialButton[] numberButtons = {
                bindingNumSys.btn0, bindingNumSys.btn1, bindingNumSys.btn2, bindingNumSys.btn3, bindingNumSys.btn4,
                bindingNumSys.btn5, bindingNumSys.btn6, bindingNumSys.btn7, bindingNumSys.btn8, bindingNumSys.btn9
        };
        for (MaterialButton button : numberButtons) {
            button.setOnClickListener(v -> appendToNumSysInput(button.getText().toString()));
        }
        bindingNumSys.btnDot.setOnClickListener(v -> { /* Для целочисленного конвертера точка игнорируется */ });
        bindingNumSys.btnAc.setOnClickListener(v -> clearNumSysInput());
        bindingNumSys.btnBackspace.setOnClickListener(v -> backspaceNumSysInput());
        bindingNumSys.btnEquals.setOnClickListener(v -> convertNumberBase());
    }

    private void appendToNumSysInput(String value) {
        if (currentInputForNumSys.equals(getString(R.string.initial_result_value))) {
            currentInputForNumSys = value;
        } else {
            currentInputForNumSys += value;
        }
        updateNumSysDisplay();
    }

    private void clearNumSysInput() {
        currentInputForNumSys = getString(R.string.initial_result_value);
        updateNumSysDisplay();
    }

    private void backspaceNumSysInput() {
        if (currentInputForNumSys.length() > 1) {
            currentInputForNumSys = currentInputForNumSys.substring(0, currentInputForNumSys.length() - 1);
        } else {
            currentInputForNumSys = getString(R.string.initial_result_value);
        }
        updateNumSysDisplay();
    }

    private boolean isValidCharForBase(char ch, int base) {
        if (Character.isDigit(ch)) return (ch - '0') < base;
        if (Character.isAlphabetic(ch)) return Character.toUpperCase(ch) - 'A' + 10 < base;
        return false;
    }

    private void convertNumberBase() {
        String fromBaseStr = bindingNumSys.fromBaseEditText.getText().toString();
        String toBaseStr = bindingNumSys.toBaseEditText.getText().toString();
        if (TextUtils.isEmpty(fromBaseStr) || TextUtils.isEmpty(toBaseStr)) {
            showError(getString(R.string.error_base_range)); return;
        }
        try {
            int fromBase = Integer.parseInt(fromBaseStr);
            int toBase = Integer.parseInt(toBaseStr);
            if (fromBase < 2 || fromBase > 36 || toBase < 2 || toBase > 36) {
                showError(getString(R.string.error_base_range)); return;
            }
            for (char ch : currentInputForNumSys.toUpperCase().toCharArray()) {
                if (!isValidCharForBase(ch, fromBase)) {
                    showError(getString(R.string.error_invalid_char_for_base) + " (осн. " + fromBase + ")"); return;
                }
            }
            if (currentInputForNumSys.isEmpty() || (currentInputForNumSys.equals(getString(R.string.initial_result_value)) && currentInputForNumSys.length() > 1) ){
                if (!currentInputForNumSys.equals("0")) {
                    showError(getString(R.string.error_invalid_input)); return;
                }
            }
            long decimalValue = currentInputForNumSys.equals("0") ? 0 : Long.parseLong(currentInputForNumSys, fromBase);
            String convertedNumber = Long.toString(decimalValue, toBase).toUpperCase();
            bindingNumSys.resultTextView.setText(convertedNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_input));
        } catch (Exception e) {
            showError(getString(R.string.error_generic));
        }
    }

    private void showError(String message) {
        if (isNumberSystemMode && bindingNumSys != null) {
            bindingNumSys.resultTextView.setText(message);
        } else if (!isNumberSystemMode && bindingStdCalc != null) {
            bindingStdCalc.standardResultTextView.setText(message);
        }
    }

    private void loadStandardCalculatorView() {
        bindingStdCalc = ActivityStandardCalculatorBinding.inflate(getLayoutInflater());
        setContentView(bindingStdCalc.getRoot());
        bindingNumSys = null;
        isNumberSystemMode = false;
        updateStandardDisplay();
        setupStandardCalculatorButtons();
        bindingStdCalc.btnSwitch.setOnClickListener(v -> loadNumberSystemCalculatorView());
    }

    private void setupStandardCalculatorButtons() {
        bindingStdCalc.btn0.setOnClickListener(v -> appendToExpression("0"));
        bindingStdCalc.btn1.setOnClickListener(v -> appendToExpression("1"));
        bindingStdCalc.btn2.setOnClickListener(v -> appendToExpression("2"));
        bindingStdCalc.btn3.setOnClickListener(v -> appendToExpression("3"));
        bindingStdCalc.btn4.setOnClickListener(v -> appendToExpression("4"));
        bindingStdCalc.btn5.setOnClickListener(v -> appendToExpression("5"));
        bindingStdCalc.btn6.setOnClickListener(v -> appendToExpression("6"));
        bindingStdCalc.btn7.setOnClickListener(v -> appendToExpression("7"));
        bindingStdCalc.btn8.setOnClickListener(v -> appendToExpression("8"));
        bindingStdCalc.btn9.setOnClickListener(v -> appendToExpression("9"));
        bindingStdCalc.btnDot.setOnClickListener(v -> appendToExpression("."));
        bindingStdCalc.btnPlus.setOnClickListener(v -> appendToExpression("+"));
        bindingStdCalc.btnMinus.setOnClickListener(v -> appendToExpression("-"));
        bindingStdCalc.btnMultiply.setOnClickListener(v -> appendToExpression("×"));
        bindingStdCalc.btnDivide.setOnClickListener(v -> appendToExpression("÷"));
        bindingStdCalc.btnPercent.setOnClickListener(v -> appendToExpression("%"));
        bindingStdCalc.btnPower.setOnClickListener(v -> appendToExpression("^"));
        bindingStdCalc.btnOpenBracket.setOnClickListener(v -> appendToExpression("("));
        bindingStdCalc.btnCloseBracket.setOnClickListener(v -> appendToExpression(")"));
        bindingStdCalc.btnClear.setOnClickListener(v -> clearExpression());
        bindingStdCalc.btnBackspace.setOnClickListener(v -> backspaceExpression());
        bindingStdCalc.btnEquals.setOnClickListener(v -> evaluateExpression());
        bindingStdCalc.btnPlusMinus.setOnClickListener(v -> toggleSignExpression());
    }

    private void appendToExpression(String token) {
        if (currentExpression.length() == 0 && getString(R.string.initial_result_value).equals(bindingStdCalc.standardResultTextView.getText().toString())) {
            if (token.equals(".") || token.equals("+") || token.equals("-") || token.equals("×") || token.equals("÷") || token.equals("%") || token.equals("^") || token.equals(")")) {
                if(!token.equals("(")) { // Скобку можно первой
                    currentExpression.append(getString(R.string.initial_result_value));
                }
            }
        }
        if (currentExpression.toString().equals(getString(R.string.initial_result_value)) && Character.isDigit(token.charAt(0)) && token.length() == 1 && !token.equals("0")) {
            currentExpression.setLength(0);
        }
        if (currentExpression.toString().equals("0") && token.equals("0")) {
            // не добавлять
        } else {
            currentExpression.append(token);
        }
        updateStandardDisplay();
    }

    private void clearExpression() {
        currentExpression.setLength(0);
        updateStandardDisplay();
    }

    private void backspaceExpression() {
        if (currentExpression.length() > 0) {
            currentExpression.deleteCharAt(currentExpression.length() - 1);
        }
        updateStandardDisplay();
    }

    private void toggleSignExpression() {
        String exprStr = currentExpression.toString();
        if (exprStr.equals(getString(R.string.initial_result_value)) || exprStr.isEmpty()) {
            currentExpression.setLength(0);
            currentExpression.append("(-");
        } else {
            try {
                double number = Double.parseDouble(exprStr); // Попытка парсить всё выражение
                number = -number;
                currentExpression.setLength(0);
                if (number == (long) number) {
                    currentExpression.append(String.format("%d", (long) number));
                } else {
                    currentExpression.append(String.format("%s", number));
                }
            } catch (NumberFormatException e) {
                // Не простое число, добавляем "(-" для ввода отрицательного числа или субвыражения
                // или если уже есть что-то, ищем последний введенный сегмент (сложно)
                // Простейший вариант: если заканчивается на оператор или '(', добавляем '-'
                if (currentExpression.length() > 0) {
                    char lastChar = currentExpression.charAt(currentExpression.length() - 1);
                    if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/' || lastChar == '^' || lastChar == '(') {
                        appendToExpression("-"); // Для унарного минуса
                    } else {
                        appendToExpression("*(-1)"); // Умножить последнее на -1 (если это число) или сломается
                        // Более безопасно: appendToExpression("(-");
                    }
                } else {
                    appendToExpression("(-");
                }
            }
        }
        updateStandardDisplay();
    }


    private void evaluateExpression() {
        String expressionToEvaluate = currentExpression.toString();
        if (TextUtils.isEmpty(expressionToEvaluate) || expressionToEvaluate.equals(getString(R.string.initial_result_value))) {
            return;
        }

        try {
            // Замена пользовательских символов на стандартные для exp4j
            expressionToEvaluate = expressionToEvaluate.replace('×', '*');
            expressionToEvaluate = expressionToEvaluate.replace('÷', '/');
            // exp4j по умолчанию использует '^' для степени.

            // Обработка процента: "N%" становится "(N/100.0)"
            // Важно: это простое преобразование. "A + B%" как "A + A * B/100" здесь не обрабатывается.
            Pattern percentPattern = Pattern.compile("([0-9.]+)\\%");
            Matcher matcher = percentPattern.matcher(expressionToEvaluate);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "(" + matcher.group(1) + "/100.0)");
            }
            matcher.appendTail(sb);
            expressionToEvaluate = sb.toString();

            // Проверка на висячие операторы в конце строки, exp4j их не любит
            if (expressionToEvaluate.endsWith("*") || expressionToEvaluate.endsWith("/") ||
                    expressionToEvaluate.endsWith("+") || expressionToEvaluate.endsWith("-") ||
                    expressionToEvaluate.endsWith("^") ) {
                showError(getString(R.string.error_invalid_expression) + ": оператор в конце");
                return;
            }


            Expression expression = new ExpressionBuilder(expressionToEvaluate).build();
            double result = expression.evaluate();

            String resultStr;

            if (Double.isInfinite(result) || Double.isNaN(result)) {
                showError(getString(R.string.error_invalid_result)); // например, деление на ноль
                return;
            }

            if (result == (long) result) { // Проверка на целое число
                resultStr = String.valueOf((long) result);
            } else {
                // Опционально: форматирование double до определенного количества знаков
                resultStr = String.valueOf(result);
            }

            currentExpression.setLength(0);
            currentExpression.append(resultStr);
            updateStandardDisplay();

        } catch (IllegalArgumentException e) { // exp4j бросает для ошибок синтаксиса, неизвестных функций/операторов
            showError(getString(R.string.error_invalid_expression) + ": " + e.getMessage());
        } catch (ArithmeticException e) { // например, деление на ноль, если exp4j его так обрабатывает
            showError(getString(R.string.error_division_by_zero) + ": " + e.getMessage());
        }
        catch (Exception e) { // Другие возможные ошибки
            showError(getString(R.string.error_generic_eval_error) + ": " + e.getMessage());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_NUM_SYS_MODE, isNumberSystemMode);

        if (isNumberSystemMode && bindingNumSys != null) {
            editor.putString(KEY_FROM_BASE, bindingNumSys.fromBaseEditText.getText().toString());
            editor.putString(KEY_TO_BASE, bindingNumSys.toBaseEditText.getText().toString());
            editor.putString(KEY_NUM_SYS_INPUT, currentInputForNumSys);
        } else {
            editor.putString(KEY_STD_EXPRESSION, currentExpression.toString());
        }
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_NUM_SYS_MODE, isNumberSystemMode);

        if (isNumberSystemMode) {
            outState.putString(KEY_NUM_SYS_INPUT, currentInputForNumSys);
            if (bindingNumSys != null) {
                outState.putString(KEY_FROM_BASE, bindingNumSys.fromBaseEditText.getText().toString());
                outState.putString(KEY_TO_BASE, bindingNumSys.toBaseEditText.getText().toString());
            }
        } else {
            outState.putString(KEY_STD_EXPRESSION, currentExpression.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Состояние уже восстановлено в onCreate
    }
}