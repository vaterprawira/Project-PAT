package com.example.concertapp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, nameEditText, emailEditText, phoneEditText;
    private Button submitButton;
    private int registerId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        nameEditText = findViewById(R.id.name_text);
        emailEditText = findViewById(R.id.email);
        phoneEditText = findViewById(R.id.phone_text);
        submitButton = findViewById(R.id.submit_button);

        // Get the registerId from the intent
        registerId = getIntent().getIntExtra("registerId", -1);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String phone = phoneEditText.getText().toString();

                new UpdateUserDetailsTask().execute(registerId, username, password, name, email, phone);
            }
        });
    }

    private class UpdateUserDetailsTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            int userId = (int) params[0];
            String username = (String) params[1];
            String password = (String) params[2];
            String name = (String) params[3];
            String email = (String) params[4];
            String phone = (String) params[5];

            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/register/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject userData = new JSONObject();
                userData.put("username", username);
                userData.put("pass", password);
                userData.put("nama", name);
                userData.put("email", email);
                userData.put("telepon", phone);

                OutputStream os = connection.getOutputStream();
                os.write(userData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                finish();
            } else {
                // Show an error message (Toast or Snackbar)
            }
        }
    }
}
