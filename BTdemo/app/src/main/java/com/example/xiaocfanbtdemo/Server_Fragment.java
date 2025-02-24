package com.example.xiaocfanbtdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.example.xiaocfanbtdemo.databinding.FragmentServerBinding;

public class Server_Fragment extends Fragment {
    private static final String TAG = "Server";
    private BluetoothAdapter mBluetoothAdapter;
    private FragmentServerBinding binding;
    private AcceptThread acceptThread;

    public Server_Fragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServerBinding.inflate(inflater, container, false);

        binding.startServer.setOnClickListener(v -> {
            logthis("Starting server\n");
            startServer();
        });

        binding.hi.setEnabled(false);
        binding.wsup.setEnabled(false);
        binding.bye.setEnabled(false);

        binding.hi.setOnClickListener(v -> {
            if (acceptThread != null) {
                acceptThread.sendMessage("Hi");
            }
        });

        binding.wsup.setOnClickListener(v -> {
            if (acceptThread != null) {
                acceptThread.sendMessage("What's up");
            }
        });

        binding.bye.setOnClickListener(v -> {
            if (acceptThread != null) {
                acceptThread.sendMessage("Bye");
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            logthis("No bluetooth device.\n");
            binding.startServer.setEnabled(false);
        }

        return binding.getRoot();
    }

    public void startServer() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private BluetoothSocket socket;
        private PrintWriter out;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MainActivity.APP_NAME, MainActivity.APP_UUID);
            } catch (IOException e) {
                logthis("Failed to start server\n");
            }
            mmServerSocket = tmp;
        }

        public void run() {
            try {
                logthis("Waiting for connection...\n");
                socket = mmServerSocket.accept();
                logthis("Client connected!\n");

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                requireActivity().runOnUiThread(() -> {
                    binding.hi.setEnabled(true);
                    binding.wsup.setEnabled(true);
                    binding.bye.setEnabled(true);
                });
            } catch (IOException e) {
                logthis("Failed to accept connection\n");
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
                out.flush();
                logthis("Sent: " + message + "\n");
            }
        }
    }

    void logthis(String item) {
        Log.v(TAG, item);
        requireActivity().runOnUiThread(() -> binding.svOutput.append(item));
    }
}
