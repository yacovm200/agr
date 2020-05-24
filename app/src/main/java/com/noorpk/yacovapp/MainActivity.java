package com.noorpk.yacovapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private Button btn_logout;
    private Button btn_uploadimage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getResources().getString(R.string.prefLoginState),"loggedout");
                editor.apply();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(MainActivity.this, Login.class));
                finish();
            }
        });

        btn_uploadimage = findViewById(R.id.btn_uploadimage);
        btn_uploadimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, uploadimage.class));
            }
        });

        ((Button) findViewById(R.id.googlemaps)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Maps.class));
            }
        });
    }
}
