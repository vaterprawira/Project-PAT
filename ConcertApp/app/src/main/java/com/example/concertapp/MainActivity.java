package com.example.concertapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private int registerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the registerId from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("registerId")) {
            registerId = intent.getIntExtra("registerId", -1);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_event) {
                    selectedFragment = new EventFragment();
                } else if (itemId == R.id.nav_pesanan) {
                    selectedFragment = new PesananFragment();
                    if (intent != null && intent.hasExtra("orderId")) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("orderId", intent.getIntExtra("orderId", -1));
                        selectedFragment.setArguments(bundle);
                    }
                } else if (itemId == R.id.nav_user) {
                    selectedFragment = new UserFragment();
                    // Pass the registerId to the UserFragment
                    if (registerId != -1) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("registerId", registerId);
                        selectedFragment.setArguments(bundle);
                    }
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }

                return true;
            }
        });

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_event); // Change to the default selected menu
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
