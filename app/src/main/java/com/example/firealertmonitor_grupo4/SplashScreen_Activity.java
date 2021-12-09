package com.example.firealertmonitor_grupo4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

@SuppressLint("CustomSplashScreen")
public class SplashScreen_Activity extends Activity {
    private ImageView img_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        img_logo = findViewById(R.id.img_splash);
        img_logo.setImageResource(R.drawable.logo_app);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen_Activity.this, Login_Activity.class);
                startActivity(intent);
            }
        },2500);

    }
}