package com.onroadvehicleassistance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {
    public static int SLEEP_TIME = 3*1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        splashToMain();
    }

    private void splashToMain() {
        /****** Create Thread that will sleep for 3 seconds****/
        Thread background = new Thread() {
            @Override
            public void run() {
                try {
                    // Thread will sleep for 3 seconds
                    sleep(SLEEP_TIME);

                    // After 3 seconds redirect to another intent
                    Intent i=new Intent(getBaseContext(),MainActivity.class);
                    startActivity(i);

                    //Remove activity
                    finish();
                } catch (Exception e) {

                }
            }
        };
        // start thread
        background.start();
    }
}