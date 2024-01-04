package com.example.rokuremote;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        SharedPreferences preferences = getSharedPreferences("MySettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        EditText baseAddr = findViewById(R.id.editTextRokuAddr);
        EditText basePort = findViewById(R.id.editTextRokuPort);
        baseAddr.setText(preferences.getString("roku_addr", ""));

        Button saveBtn = findViewById(R.id.buttonApply);
        saveBtn.setOnClickListener(v -> {
            editor.putString("roku_addr", baseAddr.getText().toString());
            editor.putString("roku_port", basePort.getText().toString());
            editor.apply();
            finish();
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}