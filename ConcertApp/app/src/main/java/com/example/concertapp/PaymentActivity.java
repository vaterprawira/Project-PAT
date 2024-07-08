package com.example.concertapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaymentActivity extends AppCompatActivity {

    private RadioGroup paymentMethodGroup;
    private TextView totalPriceTextView;
    private Button submitButton;
    private int orderId;
    private String[] paymentMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentMethodGroup = findViewById(R.id.payment_method_group);
        totalPriceTextView = findViewById(R.id.total_price);
        submitButton = findViewById(R.id.submit_button);

        orderId = getIntent().getIntExtra("orderId", -1);

        new FetchOrderDetailsTask().execute(orderId);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPayment();
            }
        });
    }

    private void submitPayment() {
        int selectedMethodId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedMethodId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedMethod = findViewById(selectedMethodId).getTag().toString();
        new SubmitPaymentTask().execute(orderId, selectedMethod);
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
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject response = jsonObject.getJSONObject("response");
                    int totalHarga = response.getInt("total_harga");
                    JSONArray banksArray = response.getJSONArray("bank");

                    totalPriceTextView.setText("Total Harga: " + totalHarga);

                    paymentMethods = new String[banksArray.length()];
                    for (int i = 0; i < banksArray.length(); i++) {
                        String bank = banksArray.getString(i);
                        paymentMethods[i] = bank;

                        RadioButton radioButton = new RadioButton(PaymentActivity.this);
                        radioButton.setText(bank);
                        radioButton.setTag(bank);
                        paymentMethodGroup.addView(radioButton);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PaymentActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PaymentActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SubmitPaymentTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            int orderId = (int) params[0];
            String metodePembayaran = (String) params[1];
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/payment");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("orderId", orderId);
                jsonBody.put("metode_pembayaran", metodePembayaran);

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
                    Log.d("PaymentActivity", "Response: " + result);
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("payment_id")) {
                        int paymentId = jsonObject.getInt("payment_id");
                        Intent intent = new Intent(PaymentActivity.this, PaymentInfoActivity.class);
                        intent.putExtra("paymentId", paymentId);
                        startActivity(intent);
                    } else {
                        throw new Exception("Unexpected response: " + result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PaymentActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PaymentActivity.this, "Failed to submit payment", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
