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
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaymentInfoActivity extends AppCompatActivity {

    private TextView orderIdTextView;
    private TextView metodePembayaranTextView;
    private TextView virtualAccountTextView;
    private TextView statusTextView;
    private Button submitButton;
    private int orderId;
    private Integer paymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_info);

        orderIdTextView = findViewById(R.id.order_id);
        metodePembayaranTextView = findViewById(R.id.metode_pembayaran);
        virtualAccountTextView = findViewById(R.id.virtual_account);
        statusTextView = findViewById(R.id.status);
        submitButton = findViewById(R.id.submit_button);

        paymentId = getIntent().getIntExtra("paymentId", -1);
        if (paymentId == -1) {
            paymentId = null;  // If paymentId is -1, set it to null
        }

        new FetchPaymentDetailsTask().execute(paymentId);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConfirmOrderTask().execute(orderId, paymentId);
            }
        });
    }

    private class FetchPaymentDetailsTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int paymentId = params[0];
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/payment/" + paymentId);
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
                    JSONObject response = jsonObject.getJSONObject("response").getJSONArray("events").getJSONObject(0);
                    orderId = response.getInt("orderId");
                    String metodePembayaran = response.getString("metode_pembayaran");
                    String virtualAccount = response.getString("virtual_account");
                    String status = response.getString("status");

                    orderIdTextView.setText("Order ID: " + orderId);
                    metodePembayaranTextView.setText("Metode Pembayaran: " + metodePembayaran);
                    virtualAccountTextView.setText("Virtual Account: " + virtualAccount);
                    statusTextView.setText("Status: " + status);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PaymentInfoActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PaymentInfoActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ConfirmOrderTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            int orderId = (int) params[0];
            Integer paymentId = (Integer) params[1];
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/confirm");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("orderId", orderId);
                jsonBody.put("paymentId", paymentId);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                writer.write(jsonBody.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    return content.toString();
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    return content.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    Log.d("PaymentInfoActivity", "Response: " + result);
                    if (result.contains("Pembayaran berhasil") || result.contains("Pembayaran sedang dikonfirmasi")) {
                        Intent intent = new Intent(PaymentInfoActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "PesananFragment");
                        intent.putExtra("orderId", orderId);  // Passing orderId to PesananFragment
                        startActivity(intent);
                    } else {
                        throw new Exception("Unexpected response: " + result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PaymentInfoActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PaymentInfoActivity.this, "Failed to submit confirmation", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
