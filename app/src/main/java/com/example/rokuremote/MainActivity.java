package com.example.rokuremote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CombinedVibration;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.annotation.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        RelativeLayout mainLayout = findViewById(R.id.relativeLayout);
        ImageButton playBtn = findViewById(R.id.pushButtonPlay);
        ImageButton homeBtn = findViewById(R.id.pushButtonHome);
        ImageButton backBtn = findViewById(R.id.pushButtonBack);
        ImageButton upBtn = findViewById(R.id.pushButtonUp);
        ImageButton downBtn = findViewById(R.id.pushButtonDown);
        ImageButton leftBtn = findViewById(R.id.pushButtonLeft);
        ImageButton rightBtn = findViewById(R.id.pushButtonRight);
        Button okBtn = findViewById(R.id.pushButtonOk);
        ImageButton volumeUpBtn = findViewById(R.id.pushButtonVolumeUp);
        ImageButton volumeDownBtn = findViewById(R.id.pushButtonVolumeDown);
        ImageButton muteBtn = findViewById(R.id.pushButtonVolumeMute);
        ImageButton rewindBtn = findViewById(R.id.pushButtonRewind);
        ImageButton forwardBtn = findViewById(R.id.pushButtonForward);
        ImageButton settingsBtn = findViewById(R.id.pushButtonSettings);
        ImageButton keyboardBtn = findViewById(R.id.pushButtonKeyboard);

        ImageButton powerBtn = findViewById(R.id.pushButtonPower);
        initSettings();


        HttpAsyncTask httpAsyncTask = new HttpAsyncTask();

        mainLayout.setOnKeyListener((view, i, keyEvent) -> {
            if(keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/backspace", "");
                    System.out.println("Key pressed: " + keyEvent.getKeyCode() + ", Backspace");
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
                    httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/Lit_%20", "");
                    System.out.println("Key pressed: " + keyEvent.getKeyCode() + ", Space");
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    mainLayout.setFocusableInTouchMode(false);
                }else{
                    String inputChar = String.valueOf(getCharFromKeyCode(keyEvent.getKeyCode()));
                    httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/Lit_" + inputChar, "");
                    System.out.println("Key pressed: " + keyEvent.getKeyCode() + ", " + inputChar);
                }
            }

            return true;
        });

        playBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/play", "");
                vibrate(50);
        });
        homeBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/home", "");
                vibrate(50);
        });
        backBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/back", "");
                vibrate(50);
        });
        upBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/up", "");
                vibrate(50);
        });
        downBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/down", "");
                vibrate(50);
        });
        leftBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/left", "");
                vibrate(50);
        });
        rightBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/right", "");
                vibrate(50);
        });
        okBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/select", "");
                vibrate(50);
        });
        volumeUpBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/volumeUp", "");
                vibrate(50);
        });
        volumeDownBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/volumeDown", "");
                vibrate(50);
        });
        muteBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/volumeMute", "");
                vibrate(50);
        });
        rewindBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/rev", "");
                vibrate(50);
        });
        forwardBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/fwd", "");
                vibrate(50);
        });
        settingsBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/info", "");
                vibrate(50);
        });
        settingsBtn.setOnLongClickListener(view -> {
            vibrate(200);
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        });
        powerBtn.setOnClickListener(view -> {
                httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/power", "");
                vibrate(50);
        });
        keyboardBtn.setOnClickListener(view -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                mainLayout.setFocusableInTouchMode(true);
                mainLayout.requestFocus();

                vibrate(50);
        });
    }
    private char getCharFromKeyCode(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        return (char) event.getUnicodeChar();
    }
    private String getBaseAddr() {
        return "http://" +
                getSharedPreferences(getString(R.string.shared_settings_key), MODE_PRIVATE)
                        .getString("roku_addr", getString(R.string.roku_addr)) +
                ":" +
                getSharedPreferences(getString(R.string.shared_settings_key), MODE_PRIVATE)
                        .getString("roku_port", getString(R.string.roku_port));
    }

    private void initSettings() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.shared_settings_key), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Set default values
        if (!preferences.contains("roku_addr")) {
            editor.putString("roku_addr", getString(R.string.roku_addr));
        }
        if (!preferences.contains("roku_port")) {
            editor.putString("roku_port", getString(R.string.roku_port));
        }
        editor.apply();
    }
    private void vibrate(int ms) {
        // Get the Vibrator service
        VibratorManager vibrator = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);

        // Check if the device has a vibrator
        if (vibrator != null){
            // Vibrate for a short duration (you can adjust the duration)
            // Vibrate with vibration effect (API level 26 and above)
            vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)));
        }
    }
}