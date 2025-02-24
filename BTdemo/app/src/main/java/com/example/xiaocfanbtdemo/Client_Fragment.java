package com.example.xiaocfanbtdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import com.example.xiaocfanbtdemo.databinding.FragmentClientBinding;

public class Client_Fragment extends Fragment {
    private static final String TAG = "ClientFragment";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private FragmentClientBinding binding;

    public Client_Fragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientBinding.inflate(inflater, container, false);

        binding.whichDevice.setOnClickListener(v -> querypaired());

        binding.startClient.setEnabled(false);
        binding.startClient.setOnClickListener(v -> {
            logthis("Starting client\n");
            startClient();
        });

        // 获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            logthis("No bluetooth device.\n");
            binding.startClient.setEnabled(false);
            binding.whichDevice.setEnabled(false);
        }

        return binding.getRoot();
    }

    public void querypaired() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            logthis("At least 1 paired device\n");
            final BluetoothDevice[] blueDev = new BluetoothDevice[pairedDevices.size()];
            String[] items = new String[blueDev.length];
            int i = 0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                items[i] = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                logthis("Device: " + items[i] + "\n");
                i++;
            }

            new android.app.AlertDialog.Builder(getActivity())
                    .setTitle("Choose Bluetooth Device:")
                    .setSingleChoiceItems(items, -1, (dialog, item) -> {
                        dialog.dismiss();
                        device = blueDev[item];
                        binding.whichDevice.setText("Device: " + blueDev[item].getName());
                        binding.startClient.setEnabled(true);
                    })
                    .create()
                    .show();
        }
    }

    public void startClient() {
        if (device != null) {
            new Thread(new ConnectThread(device)).start();
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        private final BluetoothDevice mmDevice;
        private BufferedReader in;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            try {
                socket = device.createRfcommSocketToServiceRecord(MainActivity.APP_UUID);
            } catch (IOException e) {
                logthis("Client connection failed: " + e.getMessage() + "\n");
            }
        }

        public void run() {
            try {
                socket.connect();
                logthis("Connected to server!\n");

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    String message = in.readLine();
                    if (message == null) break;
                    logthis("Received: " + message + "\n");
                }
            } catch (IOException e) {
                logthis("Connection lost\n");
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    logthis("Unable to close socket\n");
                }
            }
        }
    }

    void logthis(String item) {
        Log.v(TAG, item);
        requireActivity().runOnUiThread(() -> binding.ctOutput.append(item));
    }
}
