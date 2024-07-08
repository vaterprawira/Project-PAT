package com.example.concertapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserFragment extends Fragment {

    private TextView usernameTextView, nameTextView, emailTextView, phoneTextView;
    private Button updateButton;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        usernameTextView = view.findViewById(R.id.username);
        nameTextView = view.findViewById(R.id.name_text);
        emailTextView = view.findViewById(R.id.email);
        phoneTextView = view.findViewById(R.id.phone_text);
        updateButton = view.findViewById(R.id.update_button);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
                intent.putExtra("registerId", getArguments().getInt("registerId"));
                startActivity(intent);
            }
        });

        int userId = getArguments().getInt("registerId", -1);
        if (userId != -1) {
            new FetchUserDetailsTask().execute(userId);
        }

        return view;
    }

    private class FetchUserDetailsTask extends AsyncTask<Integer, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Integer... params) {
            int userId = params[0];
            try {
                URL url = new URL("http://192.168.68.46:8000/api/pat/register/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                return new JSONObject(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response != null) {
                try {
                    JSONObject user = response.getJSONObject("response").getJSONArray("events").getJSONObject(0);
                    usernameTextView.setText(user.getString("username"));
                    nameTextView.setText(user.getString("nama"));
                    emailTextView.setText(user.getString("email"));
                    phoneTextView.setText(user.getString("telepon"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
