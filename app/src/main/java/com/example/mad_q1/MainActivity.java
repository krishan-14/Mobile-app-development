package com.example.mad_q1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText amountInput;
    private Spinner fromSpinner, toSpinner;
    private TextView resultText;
    private String[] currencies = {"INR", "USD", "JPY", "EUR"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amountInput = findViewById(R.id.amountInput);
        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner = findViewById(R.id.toSpinner);
        resultText = findViewById(R.id.resultText);
        Button btnSettings = findViewById(R.id.btnSettings);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        // Instant conversion logic
        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { convert(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // Open Settings screen
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    private void convert() {
        if (amountInput.getText().toString().isEmpty()) return;
        double amt = Double.parseDouble(amountInput.getText().toString());
        String f = fromSpinner.getSelectedItem().toString();
        String t = toSpinner.getSelectedItem().toString();

        double inr = 1.0, usd = 0.012, jpy = 1.80, eur = 0.011;
        double base = (f.equals("INR")) ? amt : (f.equals("USD")) ? amt/usd : (f.equals("JPY")) ? amt/jpy : amt/eur;
        double res = (t.equals("INR")) ? base*inr : (t.equals("USD")) ? base*usd : (t.equals("JPY")) ? base*jpy : base*eur;

        resultText.setText(String.format("%.2f", res));
    }
}