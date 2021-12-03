package com.example.bagwashbuddies;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bagwashbuddies.databinding.ActivityLaundryBinding;
import com.example.bagwashbuddies.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class LaundryActivity extends AppCompatActivity {
    private ActivityLaundryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Log.d("Curr user", "Curr user's name " + auth.getCurrentUser().getDisplayName());
        Log.d("Curr user", "Curr user's email " + auth.getCurrentUser().getEmail());
        Log.d("Curr user", "Curr user's id " + auth.getCurrentUser().getUid());

        RequestQueue reqQueue = Volley.newRequestQueue(LaundryActivity.this);
        reqQueue.getCache().clear();

        // Get weather info
        loadWeather();

        binding = ActivityLaundryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
    }

    private void loadWeather() {
        final String URL = "https://api.weatherbit.io/v2.0/current?postal_code=99732&country=US&units=I&key=61e2913ed3da452f8d1e19d17ee1a0fe";

        RequestQueue queue = Volley.newRequestQueue(LaundryActivity.this);

        ResponseHandler jsonObjectRequest = new ResponseHandler(Request.Method.GET, URL, null, response -> {
            try {
                JSONArray data = response.getJSONArray("data");
                String temp = data.getJSONObject(0).getString("temp");
                String weather = data.getJSONObject(0).getString("weather");

                JSONObject weatherObj = new JSONObject(weather);
                String icon = "https://www.weatherbit.io/static/img/icons/" + weatherObj.getString("icon") + ".png";
                String description = weatherObj.getString("description");

                TextView weather_tv = findViewById(R.id.weather_tv);
                weather_tv.setText(temp + "° Fahrenheit\n" + description);

                ImageView weather_iv = findViewById(R.id.weather_image_view);
                Picasso.get().load(icon).into(weather_iv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Throwable::printStackTrace);

        queue.add(jsonObjectRequest);
    }

}