package com.bhayu.robot;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    private final int BLUE = Color.rgb(0, 190, 255);
    private final int BACKGROUND = Color.rgb(5, 10, 22);

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private TextView connectionStatus;
    private TextView responseText;

    private final UUID bluetoothUUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        createInterface();
    }

    private void createInterface() {

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(22, 30, 22, 20);
        main.setBackgroundColor(BACKGROUND);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(main);

        TextView title = new TextView(this);
        title.setText("B H A Y U");
        title.setTextColor(BLUE);
        title.setTextSize(34);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        main.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("INTELLIGENT ROBOT ASSISTANT");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 5, 0, 25);
        main.addView(subtitle);

        TextView core = new TextView(this);
        core.setText("◉");
        core.setTextColor(BLUE);
        core.setTextSize(100);
        core.setGravity(Gravity.CENTER);
        main.addView(core);

        connectionStatus = new TextView(this);
        connectionStatus.setText("● DISCONNECTED");
        connectionStatus.setTextColor(Color.RED);
        connectionStatus.setTextSize(16);
        connectionStatus.setGravity(Gravity.CENTER);
        connectionStatus.setPadding(0, 15, 0, 15);
        main.addView(connectionStatus);

        Button connect = makeButton("CONNECT TO ROBOT");
        connect.setOnClickListener(v -> showBluetoothDevices());
        main.addView(connect);

        addHeading(main, "ROBOT MOVEMENT");

        addControlRow(main, "⬆ FORWARD", "⬅ LEFT", "➡ RIGHT");
        addControlRow(main, "⬇ BACKWARD", "■ STOP", " ");

        addHeading(main, "ROBOT ARM");

        addControlRow(main, "ARM UP", "ARM DOWN", " ");
        addControlRow(main, "GRIP OPEN", "GRIP CLOSE", " ");

        addHeading(main, "BHAYU AI");

        responseText = new TextView(this);
        responseText.setText("Hello Ashutosh! I am BHAYU.\nYour robot assistant is ready.");
        responseText.setTextColor(Color.WHITE);
        responseText.setTextSize(16);
        responseText.setPadding(12, 15, 12, 15);
        main.addView(responseText);

        Button identity = makeButton("WHO BUILT YOU?");
        identity.setOnClickListener(v ->
                responseText.setText("I was built by Ashutosh."));
        main.addView(identity);

        TextView footer = new TextView(this);
        footer.setText("BHAYU • CREATED BY ASHUTOSH");
        footer.setTextColor(BLUE);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 30, 0, 10);
        main.addView(footer);

        setContentView(scroll);
    }

    private void addHeading(LinearLayout layout, String text) {
        TextView heading = new TextView(this);
        heading.setText(text);
        heading.setTextColor(BLUE);
        heading.setTextSize(18);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setPadding(0, 25, 0, 12);
        layout.addView(heading);
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(13);
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(12, 55, 85)));
        return button;
    }

    private void addControlRow(
            LinearLayout layout, String first, String second, String third) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        addControlButton(row, first);
        addControlButton(row, second);

        if (!third.trim().isEmpty()) {
            addControlButton(row, third);
        }

        layout.addView(row);
    }

    private void addControlButton(LinearLayout row, String label) {

        Button button = makeButton(label);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, 65, 1);

        params.setMargins(3, 4, 3, 4);
        row.addView(button, params);

        button.setOnClickListener(v -> handleCommand(label));
    }

    private void handleCommand(String label) {

        String command = "";

        if (label.contains("FORWARD")) command = "F";
        else if (label.contains("BACKWARD")) command = "B";
        else if (label.contains("LEFT")) command = "L";
        else if (label.contains("RIGHT")) command = "R";
        else if (label.contains("STOP")) command = "S";
        else if (label.contains("ARM UP")) command = "ARM_UP";
        else if (label.contains("ARM DOWN")) command = "ARM_DOWN";
        else if (label.contains("GRIP OPEN")) command = "GRIP_OPEN";
        else if (label.contains("GRIP CLOSE")) command = "GRIP_CLOSE";

        sendCommand(command);
    }

    private void showBluetoothDevices() {

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= 31 &&
                checkSelfPermission("android.permission.BLUETOOTH_CONNECT")
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{"android.permission.BLUETOOTH_CONNECT"}, 100);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please turn on Bluetooth",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();

            ArrayList<BluetoothDevice> devices = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();

            for (BluetoothDevice device : paired) {
                devices.add(device);
                names.add(device.getName() + "\n" + device.getAddress());
            }

            if (devices.isEmpty()) {
                Toast.makeText(this,
                        "Pair your HC-05 in Android Bluetooth settings first",
                        Toast.LENGTH_LONG).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Select your robot")
                    .setItems(names.toArray(new String[0]),
                            (dialog, which) -> connectToDevice(devices.get(which)))
                    .setNegativeButton("Cancel", null)
                    .show();

        } catch (SecurityException e) {
            Toast.makeText(this, "Bluetooth permission required",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void connectToDevice(BluetoothDevice device) {

        connectionStatus.setText("● CONNECTING...");
        connectionStatus.setTextColor(Color.YELLOW);

        new Thread(() -> {
            try {
                BluetoothSocket socket =
                        device.createRfcommSocketToServiceRecord(bluetoothUUID);

                socket.connect();

                OutputStream stream = socket.getOutputStream();

                runOnUiThread(() -> {
                    bluetoothSocket = socket;
                    outputStream = stream;

                    connectionStatus.setText("● ROBOT CONNECTED");
                    connectionStatus.setTextColor(Color.GREEN);

                    Toast.makeText(this, "BHAYU connected!",
                            Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    connectionStatus.setText("● CONNECTION FAILED");
                    connectionStatus.setTextColor(Color.RED);

                    Toast.makeText(this, "Connection failed. Check HC-05.",
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void sendCommand(String command) {

        if (command.equals("S")) {
            // STOP is always attempted immediately.
        }

        if (outputStream == null) {
            Toast.makeText(this, "Connect to your robot first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            outputStream.write((command + "\n").getBytes());
            outputStream.flush();

        } catch (Exception e) {
            connectionStatus.setText("● CONNECTION LOST");
            connectionStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showBluetoothDevices();
        } else if (requestCode == 100) {
            Toast.makeText(this, "Bluetooth permission denied",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (Exception ignored) {
        }
    }
}
