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
        TextView deviceNameTextView = findViewById(R.id.deviceName);
        initSettings();


        HttpAsyncTask httpAsyncTask = new HttpAsyncTask();

        if(!updateDeviceName(deviceNameTextView)){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            updateDeviceName(deviceNameTextView);
        }, 0, 5, TimeUnit.SECONDS);

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
            sendKeypressCommand("play");});
        homeBtn.setOnClickListener(view -> {
            sendKeypressCommand("home");});
        backBtn.setOnClickListener(view -> {
            sendKeypressCommand("back");});
        upBtn.setOnClickListener(view -> {
            sendKeypressCommand("up");});
        downBtn.setOnClickListener(view -> {
            sendKeypressCommand("down");});
        leftBtn.setOnClickListener(view -> {
            sendKeypressCommand("left");});
        rightBtn.setOnClickListener(view -> {
            sendKeypressCommand("right");});
        okBtn.setOnClickListener(view -> {
            sendKeypressCommand("select");});
        volumeUpBtn.setOnClickListener(view -> {
            sendKeypressCommand("volumeUp");});
        volumeDownBtn.setOnClickListener(view -> {
            sendKeypressCommand("volumeDown");});
        muteBtn.setOnClickListener(view -> {
            sendKeypressCommand("volumeMute");});
        rewindBtn.setOnClickListener(view -> {
            sendKeypressCommand("rev");});
        forwardBtn.setOnClickListener(view -> {
            sendKeypressCommand("fwd");});
        settingsBtn.setOnClickListener(view -> {
            sendKeypressCommand("info");});
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
        // Get the Vibrator service
        VibratorManager vibrator = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);

        // Check if the device has a vibrator
        if (vibrator != null){
            // Vibrate for a short duration (you can adjust the duration)
            // Vibrate with vibration effect (API level 26 and above)
            vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)));
        }
    }
    private String getRokuDeviceName() throws IOException {
        HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
        String response = httpAsyncTask.makeGetRequest(getBaseAddr());
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