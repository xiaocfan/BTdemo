package com.example.xiaocfanbtdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Map;
import java.util.Set;

import com.example.xiaocfanbtdemo.databinding.FragmentHelpBinding;

public class Help_Fragment extends Fragment {

    private static final String TAG = "HelpFragment";
    private FragmentHelpBinding binding;

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ActivityResultLauncher<Intent> bluetoothActivityResultLauncher;
    private ActivityResultLauncher<String[]> permissionsLauncher;

    private String[] REQUIRED_PERMISSIONS;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelpBinding.inflate(inflater, container, false);

        setupPermissions();
        setupResultLaunchers();

        binding.helpClient.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_client, null));
        binding.helpServer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_server, null));

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!allPermissionsGranted()) {
            permissionsLauncher.launch(REQUIRED_PERMISSIONS);
        } else {
            logMessage("All permissions have been granted.");
            startBluetooth();
        }
    }

    /** Bluetooth permission */
    private void setupPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
            logMessage("Android 12+ requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT permissions.");
        } else {
            REQUIRED_PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            };
            logMessage("Android 11 or lower requires BLUETOOTH and BLUETOOTH_ADMIN permissions.");
        }
    }

    /** Init ActivityResultLauncher */
    private void setupResultLaunchers() {
        bluetoothActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        logMessage("Bluetooth is enabled.");
                        queryPairedDevices();
                    } else {
                        logMessage("Bluetooth is disabled. Please enable it.");
                    }
                }
        );

        permissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        logMessage(entry.getKey() + " permission granted: " + entry.getValue());
                        if (!entry.getValue()) allGranted = false;
                    }
                    if (allGranted) startBluetooth();
                }
        );
    }

    /** Bluetooth check and enable */
    private void startBluetooth() {
        if (mBluetoothAdapter == null) {
            logMessage("This device does not support Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            logMessage("Bluetooth is available but disabled.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivityResultLauncher.launch(enableBtIntent);
        } else {
            logMessage("Bluetooth is ready to use.");
            queryPairedDevices();
        }
    }

    /** Get paired devices */
    @SuppressLint("MissingPermission")
    private void queryPairedDevices() {
        logMessage("Paired Bluetooth Devices:");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                logMessage("Device: " + device.getName() + " - " + device.getAddress());
            }
        } else {
            logMessage("No paired Bluetooth devices found.");
        }
    }

    /** Check permission */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Log func */
    private void logMessage(String message) {
        if (binding != null) {
            binding.logger1.post(() -> binding.logger1.append(message + "\n"));
        }
        Log.d(TAG, message);
    }
}
