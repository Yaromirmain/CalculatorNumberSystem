package com.example.calculatornumbersystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.calculatornumbersystem.databinding.ActivityStandardCalculatorBinding;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class StandardCalculatorActivity extends AppCompatActivity {

    private ActivityStandardCalculatorBinding binding;
    private StringBuilder currentInput = new StringBuilder();
    private boolean lastCharIsOperator = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStandardCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupButtons();

        binding.btnSwitch.setOnClickListener(v -> {
            startActivity(new Intent(this, NumberSystemActivity.class));
            finish();
        });
    }

    private void setupButtons() {
        // Number buttons
        int[] numberButtons = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9};

        for (int buttonId : numberButtons) {
            findViewById(buttonId).setOnClickListener(v -> {
                String number = ((com.google.android.material.button.MaterialButton) v).getText().toString();
                appendToInput(number);
            });
        }

        // Operation buttons
        int[] operationButtons = {R.id.btn_plus, R.id.btn_minus, R.id.btn_multiply, R.id.btn_divide,
                R.id.btn_power, R.id.btn_percent};

        for (int buttonId : operationButtons) {
            findViewById(buttonId).setOnClickListener(v -> {
                String operatorText = ((com.google.android.material.button.MaterialButton) v).getText().toString();
                String operatorToSend;
                switch (operatorText) {
                    case "×":
                        operatorToSend = "*";
                        break;
                    case "÷":
                        operatorToSend = "/";
                        break;
                    case "xʸ":
                        operatorToSend = "^";
                        break;
                    case "%":
                        // For now, we'll append '%' and handle it in calculateResult.
                        // A more robust solution might involve parsing the number before it.
                        operatorToSend = "%";
                        break;
                    default:
                        operatorToSend = operatorText;
                        break;
                }
                handleOperator(operatorToSend);
            });
        }

        // Special buttons
        findViewById(R.id.btn_dot).setOnClickListener(v -> {
            if (!currentInput.toString().contains(".")) {
                appendToInput(".");
            }
        });

        findViewById(R.id.btn_plus_minus).setOnClickListener(v -> toggleSign());

        findViewById(R.id.btn_open_bracket).setOnClickListener(v -> appendToInput("("));
        findViewById(R.id.btn_close_bracket).setOnClickListener(v -> appendToInput(")"));

        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            currentInput.setLength(0);
            updateDisplay();
        });

        findViewById(R.id.btn_backspace).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
                updateDisplay();
            }
        });

        findViewById(R.id.btn_equals).setOnClickListener(v -> calculateResult());
    }

    private void appendToInput(String value) {
        currentInput.append(value);
        lastCharIsOperator = false;
        updateDisplay();
    }

    private void handleOperator(String operator) {
        if (currentInput.length() == 0) return;

        // Prevent multiple operators in a row, replace the last one if it was an operator
        if (lastCharIsOperator) {
            // Special handling for unary minus: if the last char was an operator,
            // and the new operator is '-', it might be a unary minus (e.g., 5*-2)
            // For simplicity, we'll just replace the operator for now.
            // For advanced unary minus, you might need to check context.
            currentInput.deleteCharAt(currentInput.length() - 1);
        }

        currentInput.append(operator);
        lastCharIsOperator = true;
        updateDisplay();
    }

    private void toggleSign() {
        if (currentInput.length() == 0) return;

        // This attempts to toggle the sign of the *last* number entered or the current result.
        // It's a simplification. A full calculator would need to parse the expression
        // to find the number to negate.
        try {
            // If the current input is a simple number, toggle its sign directly.
            double value = Double.parseDouble(currentInput.toString());
            currentInput.setLength(0);
            currentInput.append(-value);
            updateDisplay();
        } catch (NumberFormatException e) {
            // If it's not a simple number (e.g., "5+2"), try to insert a minus at the beginning
            // or apply to the last number. This is a complex logic for a basic toggle button.
            // For simplicity, let's just prepend a minus if it's not a direct number.
            // This might lead to invalid expressions like "-5+2", but depends on desired behavior.
            if (currentInput.length() > 0 && currentInput.charAt(0) == '-') {
                currentInput.deleteCharAt(0);
            } else {
                currentInput.insert(0, "-");
            }
            updateDisplay();
        }
    }

    private void updateDisplay() {
        if (currentInput.length() == 0) {
            binding.standardResultTextView.setText("0");
        } else {
            binding.standardResultTextView.setText(currentInput.toString());
        }
    }

    private void calculateResult() {
        if (currentInput.length() == 0) return;

        try {
            String expressionString = currentInput.toString();

            // Replace percentage: "number%" with "number*0.01"
            // This is a simple global replacement. For more advanced use cases (e.g., "100 + 50%"),
            // you might need a more sophisticated parsing mechanism, like regular expressions
            // to ensure it only applies to numbers directly preceding the %.
            expressionString = expressionString.replace("%", "*0.01");


            Expression expression = new ExpressionBuilder(expressionString).build();
            double result = expression.evaluate();

            currentInput.setLength(0);
            if (result == (long) result) { // Check if result is an integer
                currentInput.append((long) result);
            } else {
                currentInput.append(result);
            }
            updateDisplay();
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            binding.standardResultTextView.setText("Error");
            currentInput.setLength(0); // Clear input on error
        }
    }
}