package com.example.concertapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PesananActivity extends AppCompatActivity {

    private static final String TAG = "PesananActivity";
    private TextView orderIdTextView;
    private TextView statusTextView;
    private TextView kodeBookingTextView;
    private Button viewDetailsButton;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesanan);

        orderIdTextView = findViewById(R.id.orderId);
        statusTextView = findViewById(R.id.status);
        kodeBookingTextView = findViewById(R.id.kodeBooking);
        viewDetailsButton = findViewById(R.id.viewDetails);

        orderId = getIntent().getIntExtra("orderId", -1);

        if (orderId != -1) {
            new FetchOrderDetailsTask().execute(orderId);
        } else {
            Log.e(TAG, "orderId is invalid");
            Toast.makeText(this, "Invalid orderId", Toast.LENGTH_SHORT).show();
        }

        viewDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PesananActivity.this, MainActivity.class);
                intent.putExtra("fragment", "PesananFragment");
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        });
    }

    private class FetchOrderDetailsTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int orderId = params[0];
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/order/" + orderId);
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
                Log.e(TAG, "Error fetching order details: " + e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject response = jsonObject.getJSONObject("response");
                    String orderId = response.getString("orderId");
                    String status = response.getString("status");
                    String kodeBooking = response.getString("kode_booking");

                    orderIdTextView.setText("Order ID: " + orderId);
                    statusTextView.setText("Status: " + status);
                    kodeBookingTextView.setText("Kode Booking: " + kodeBooking);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing data: " + e.getMessage(), e);
                    Toast.makeText(PesananActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to fetch data");
                Toast.makeText(PesananActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
