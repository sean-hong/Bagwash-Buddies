package com.example.bagwashbuddies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {
                Log.d("RESULT", "GOT THIS RESULT HERE " + result);
                Intent intent = new Intent(MainActivity.this, LaundryActivity.class);
                // intent.putExtra("currUserObj", );
                startActivity(intent);
                // Handle the FirebaseAuthUIAuthenticationResult
                // ...
            });

    private Button eLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eLogin = findViewById(R.id.login_btn);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        eLogin.setOnClickListener(view -> startSignIn());

        Log.d("CURR USER", "CURR USER == " + auth.getCurrentUser());
    }

    private void startSignIn() {
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setLogo(R.drawable.ccc)
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                // ... options ...
                .build();

        signInLauncher.launch(signInIntent);
    }
}