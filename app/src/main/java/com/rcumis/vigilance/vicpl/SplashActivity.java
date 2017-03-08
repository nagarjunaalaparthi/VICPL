package com.rcumis.vigilance.vicpl;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.rcumis.vigilance.vicpl.utils.VigilancePreferenceManager;

/**
 * Created by Arjun on 23/10/2016.
 * Splash screen with logo and version name
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView versionTextView = (TextView) findViewById(R.id.version_textView);
        if (versionTextView != null) {
            versionTextView.setText(getString(R.string.version_name,BuildConfig.VERSION_NAME));
        }
        VigilancePreferenceManager.setLaunchedTime(this, System.currentTimeMillis());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this,MainActivity.class);
                startActivity(mainIntent);
                overridePendingTransition(R.anim.anim_fade_out,R.anim.anim_fade_in);
                finish();
            }
        },2000);
    }
}
