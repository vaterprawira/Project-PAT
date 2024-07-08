package com.example.concertapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private LinearLayout ticketContainer;
    private Button addButton, submitButton;
    private List<EditText> jumlahFields;
    private List<RadioGroup> kategoriFields;
    private List<String> kategoriList;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ticketContainer = findViewById(R.id.ticket_container);
        addButton = findViewById(R.id.add_button);
        submitButton = findViewById(R.id.submit_button);
        jumlahFields = new ArrayList<>();
        kategoriFields = new ArrayList<>();
        kategoriList = new ArrayList<>();
        eventId = getIntent().getIntExtra("eventId", -1);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTicketField();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitOrder();
            }
        });

        // Fetch event data
        new FetchEventDataTask().execute();
    }

    private void addTicketField() {
        View ticketView = getLayoutInflater().inflate(R.layout.item_ticket, ticketContainer, false);
        EditText jumlahField = ticketView.findViewById(R.id.jumlah_field);
        RadioGroup kategoriGroup = ticketView.findViewById(R.id.kategori_group);

        // Dynamically add radio buttons based on kategoriList
        for (String kategori : kategoriList) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(kategori);
            radioButton.setTag(kategori);
            kategoriGroup.addView(radioButton);
        }

        jumlahFields.add(jumlahField);
        kategoriFields.add(kategoriGroup);
        ticketContainer.addView(ticketView);
    }

    private void submitOrder() {
        List<TicketOrder> ticketOrders = new ArrayList<>();
        for (int i = 0; i < jumlahFields.size(); i++) {
            String jumlahStr = jumlahFields.get(i).getText().toString();
            int selectedKategoriId = kategoriFields.get(i).getCheckedRadioButtonId();
            if (selectedKategoriId == -1 || jumlahStr.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int jumlah = Integer.parseInt(jumlahStr);
            String kategori = findViewById(selectedKategoriId).getTag().toString();
            ticketOrders.add(new TicketOrder(kategori, jumlah));
        }

        new SubmitOrderTask(ticketOrders).execute();
    }

    private class FetchEventDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/event/mobile/" + eventId);
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
                    JSONArray kategoriArray = jsonObject.getJSONObject("response").getJSONArray("events").getJSONObject(0).getJSONArray("kategori");

                    for (int i = 0; i < kategoriArray.length(); i++) {
                        kategoriList.add(kategoriArray.getString(i));
                    }

                    addTicketField(); // Add initial ticket field after fetching categories

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(OrderActivity.this, "Error parsing event data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderActivity.this, "Failed to fetch event data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SubmitOrderTask extends AsyncTask<Void, Void, String> {
        private List<TicketOrder> ticketOrders;

        public SubmitOrderTask(List<TicketOrder> ticketOrders) {
            this.ticketOrders = ticketOrders;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/order");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("eventId", eventId);
                JSONArray kategoriTiketArray = new JSONArray();
                for (TicketOrder order : ticketOrders) {
                    JSONObject orderObject = new JSONObject();
                    orderObject.put("kategori", order.getKategori());
                    orderObject.put("jumlah", order.getJumlah());
                    kategoriTiketArray.put(orderObject);
                }
                jsonBody.put("kategori_tiket", kategoriTiketArray);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                writer.write(jsonBody.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                Log.d("SubmitOrderTask", "Response code: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();
                    Log.d("SubmitOrderTask", "Response: " + response.toString());
                    return response.toString();
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorContent = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        errorContent.append(line);
                    }
                    in.close();
                    Log.e("SubmitOrderTask", "Error response: " + errorContent.toString());
                    return "Failed to place order: " + errorContent.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SubmitOrderTask", "Exception: " + e.getMessage());
                return "Error placing order: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Log.d("SubmitOrderTask", "Result: " + result);
                JSONObject jsonObject = new JSONObject(result);

                if (jsonObject.getInt("status") == 200) {
                    int orderId = jsonObject.getInt("order_id");
                    Intent intent = new Intent(OrderActivity.this, PaymentActivity.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                } else {
                    String error = jsonObject.optString("error", "Order failed");
                    Toast.makeText(OrderActivity.this, "Order failed: " + error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(OrderActivity.this, "Error parsing order response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



    private class TicketOrder {
        private String kategori;
        private int jumlah;

        public TicketOrder(String kategori, int jumlah) {
            this.kategori = kategori;
            this.jumlah = jumlah;
        }

        public String getKategori() { return kategori; }
        public int getJumlah() { return jumlah; }
    }
}
