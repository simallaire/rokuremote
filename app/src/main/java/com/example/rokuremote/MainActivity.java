package com.example.rokuremote;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private String rokuDeviceName = "Roku Device";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        RelativeLayout mainLayout = findViewById(R.id.relativeLayout);
        ImageButton settingsBtn = findViewById(R.id.pushButtonSettings);
        ImageButton keyboardBtn = findViewById(R.id.pushButtonKeyboard);
        ImageButton powerBtn = findViewById(R.id.pushButtonPower);
        TextView deviceNameTextView = findViewById(R.id.deviceName);
        initSettings();

        if(!updateDeviceName(deviceNameTextView)){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            updateDeviceName(deviceNameTextView);
        }, 0, 5, TimeUnit.SECONDS);

        setKeyboardListener(mainLayout);
        bindKeypressButtons();

        settingsBtn.setOnLongClickListener(view -> {
            vibrate(200);
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        });
        powerBtn.setOnClickListener(view -> sendKeypressCommand("power"));
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

    private void setKeyboardListener(RelativeLayout mainLayout) {
        mainLayout.setOnKeyListener((view, i, keyEvent) -> {
            if(keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    try {
                        HttpHelper.makeHttpPostRequest(getBaseAddr() + "/keypress/backspace", "");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Key pressed: " + keyEvent.getKeyCode() + ", Backspace");
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_SPACE) {
                    try {
                        HttpHelper.makeHttpPostRequest(getBaseAddr() + "/keypress/Lit_%20", "");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Key pressed: " + keyEvent.getKeyCode() + ", Space");
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    mainLayout.setFocusableInTouchMode(false);
                }else{
                    String inputChar = String.valueOf(getCharFromKeyCode(keyEvent.getKeyCode()));
                    try {
                        HttpHelper.makeHttpPostRequest(getBaseAddr() + "/keypress/Lit_" + inputChar, "");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Key pressed: " + keyEvent.getKeyCode() + ", " + inputChar);
                }
            }

            return true;
        });
    }

    private void bindKeypressButtons() {
        int[] buttonIds = getResources().getIntArray(R.array.button_ids);
        String[] buttonCommands = getResources().getStringArray(R.array.button_commands);

        if (buttonIds.length != buttonCommands.length) {
            throw new IllegalStateException("button_ids and button_commands must have the same length");
        }

        for (int i = 0; i < buttonIds.length; i++) {
            int buttonId = buttonIds[i];
            String command = buttonCommands[i];
            if (findViewById(buttonId) != null) {
                findViewById(buttonId).setOnClickListener(view -> sendKeypressCommand(command));
            }
        }
    }

    private void sendKeypressCommand(String command) {
        vibrate(1);
        int repeat = 1;
        if (Objects.equals(command, "fwd") || Objects.equals(command, "rev")){
            repeat = 5;
        }
        String response = null;
        for (int i = 0; i < repeat; i++)
            try {
                response = HttpHelper.makeHttpPostRequest(getBaseAddr() + "/keypress/" + command, "");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        vibrate(25);
        Log.d("RokuCommand", "Response: " + response);
    }
    private void sendKeypressCommand(String command) {
        HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
        vibrate(10);
        String response = httpAsyncTask.doInBackground(getBaseAddr() + "/keypress/" + command, "");
        vibrate(25);
        Log.d("RokuCommand", "Response: " + response);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.getRepeatCount() == 0)
        {
           sendKeypressCommand("volumeDown");
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getRepeatCount() == 0)
        {
            sendKeypressCommand("volumeUp");
        }
        return true;
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
        VibratorManager vibrator = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        if (vibrator != null){
            vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)));
        }
    }
    private String getRokuDeviceName() throws IOException {

        String response = HttpHelper.makeGetRequest(getBaseAddr());
        Log.d("RokuInfo", "Response: " + response);
        XMLParser xmlParser = new XMLParser();
        Document document = xmlParser.parseXML(response);
        Node device = document.getElementsByTagName("device").item(0).getChildNodes().item(1);
        String deviceName = device.getFirstChild().getNodeValue();
        Log.d("RokuInfo", "Device Name: " + deviceName);
        return deviceName;
    }
    // Create recurring background task to check for updates
    @Override
    protected void onResume() {
        super.onResume();
        // Reinitialize the settings in case they were changed
        initSettings();
        try {
            rokuDeviceName = getRokuDeviceName();
            TextView deviceNameTextView = findViewById(R.id.deviceName);
            deviceNameTextView.setText(rokuDeviceName);
        } catch (IOException e) {
            Log.e("RokuInfo", "Error getting Roku device name", e);
        }
    }
    private boolean updateDeviceName(TextView deviceNameTextView) {
        int rokuDeviceNameColor = getResources().getColor(R.color.Connected_green);
        try {
            rokuDeviceName = getRokuDeviceName();
        } catch (IOException e) {
            Log.e("RokuInfo", "Error getting Roku device name", e);
            rokuDeviceName = "Roku Device";
            rokuDeviceNameColor = getResources().getColor(R.color.Not_connected_red);
            return false;
        }
        deviceNameTextView.setTextColor(rokuDeviceNameColor);
        deviceNameTextView.setText(rokuDeviceName);
        return true;
    }

}