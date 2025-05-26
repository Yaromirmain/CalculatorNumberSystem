package com.example.calculatornumbersystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.example.calculatornumbersystem.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnNumberSystem.setOnClickListener(v -> {
            startActivity(new Intent(this, NumberSystemActivity.class));
        });

        binding.btnStandardCalculator.setOnClickListener(v -> {
            startActivity(new Intent(this, StandardCalculatorActivity.class));
        });
    }
}