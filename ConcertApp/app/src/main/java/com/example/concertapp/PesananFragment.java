package com.example.concertapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PesananFragment extends Fragment {

    private TextView orderIdTextView;
    private TextView statusTextView;
    private TextView kodeBookingTextView;
    private Button refreshButton;
    private int orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pesanan, container, false);

        orderIdTextView = view.findViewById(R.id.orderId);
        statusTextView = view.findViewById(R.id.status);
        kodeBookingTextView = view.findViewById(R.id.kodeBooking);
        refreshButton = view.findViewById(R.id.refreshButton);

        Bundle bundle = getArguments();
        if (bundle != null) {
            orderId = bundle.getInt("orderId", -1);
            if (orderId != -1) {
                new FetchConfirmationDetailsTask().execute(orderId);
            } else {
                Toast.makeText(getActivity(), "Order ID is not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
        }

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderId != -1) {
                    new FetchConfirmationDetailsTask().execute(orderId);
                } else {
                    Toast.makeText(getActivity(), "Order ID is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private class FetchConfirmationDetailsTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int orderId = params[0];
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/confirm/" + orderId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();
                return content.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int orderId = jsonObject.getInt("orderId");
                    String status = jsonObject.getString("status");
                    String kodeBooking = jsonObject.getString("kode_booking");

                    orderIdTextView.setText("Order ID: " + orderId);
                    statusTextView.setText("Status: " + status);
                    kodeBookingTextView.setText("Kode Booking: " + kodeBooking);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
