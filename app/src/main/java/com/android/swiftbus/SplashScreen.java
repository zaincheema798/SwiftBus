package com.android.swiftbus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    Button btn;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        btn = findViewById(R.id.bt);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(SplashScreen.this, Main.class));
            finish();
        }
        btn.setOnClickListener(v ->  {
            Intent intent = new Intent(SplashScreen.this, Options.class);
            startActivity(intent);
            finish();
        });
    }
}