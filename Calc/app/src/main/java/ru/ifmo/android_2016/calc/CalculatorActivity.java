package ru.ifmo.android_2016.calc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public final class CalculatorActivity extends AppCompatActivity {

    private static final String TEXT_FIELD = "text field";
    private static final String CURRENT_TEXT = "current text";
    private static final String IS_DOTTED = "is dotted";

    private TextView textField;
    private StringBuilder currentText;
    private boolean isDotted;
    private Button digit;
    private Button operation;
    private Button dot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        textField = (TextView) findViewById(R.id.text_field);
        digit = (Button) findViewById(R.id.digit);
        operation = (Button) findViewById(R.id.operation);

        final Button clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentText.length() > 0) {
                    if (currentText.charAt(currentText.length() - 1) == '.') {
                        isDotted = false;
                    }
                    currentText.deleteCharAt(currentText.length() - 1);
                    if (currentText.length() > 0) {
                        textField.setText(currentText.toString());
                    } else {
                        resetStartingFields();
                    }
                }
            }
        });
        clear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                resetStartingFields();
                return true;
            }
        });

        dot = (Button) findViewById(R.id.dot);
        dot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDotted) {
                    dot = (Button) v;
                    isDotted = true;
                    initializeByZero();
                    if (!Character.isDigit(currentText.charAt(currentText.length() - 1))) {
                        updateFields("0");
                    }
                    updateFields(dot.getText().toString());
                }
            }
        });

        Button equals = (Button) findViewById(R.id.equals);
        equals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String expression = currentText.toString();
                    resetStartingFields();
                    String parsed = new Parser().parse(expression);
                    textField.setText(expression + '\n' + '\n' + parsed);
                    currentText = new StringBuilder(parsed);
                } catch (RuntimeException e) {
                    resetStartingFields();
                    textField.setText(e.getMessage());
                }
            }
        });

        resetStartingFields();
        if (savedInstanceState != null) {
            textField.setText(savedInstanceState.getString(TEXT_FIELD));
            currentText = new StringBuilder(savedInstanceState.getString(CURRENT_TEXT));
            isDotted = savedInstanceState.getBoolean(IS_DOTTED);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEXT_FIELD, textField.getText().toString());
        outState.putString(CURRENT_TEXT ,currentText.toString());
        outState.putBoolean(IS_DOTTED, isDotted);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        textField.setText(savedInstanceState.getString(TEXT_FIELD));
        currentText = new StringBuilder(savedInstanceState.getString(CURRENT_TEXT));
        isDotted = savedInstanceState.getBoolean(IS_DOTTED);
    }

    private void resetStartingFields() {
        textField.setText("");
        currentText = new StringBuilder();
        isDotted = false;
    }

    private void updateFields(String s) {
        currentText.append(s);
        textField.append(s);
    }

    private void initializeByZero() {
        if (currentText.length() == 0) {
            currentText.append("0");
            textField.append("0");
        }
    }

    public void onDigitClicked(View view) {
        digit = (Button) view;
        updateFields(digit.getText().toString());
    }

    public void onOperationClicked(View view) {
        operation = (Button) view;
        initializeByZero();
        if (Character.isDigit(currentText.charAt(currentText.length() - 1))
                || operation.getText().toString().equals("-")) {
            updateFields(operation.getText().toString());
        }
        isDotted = false;
    }
}
