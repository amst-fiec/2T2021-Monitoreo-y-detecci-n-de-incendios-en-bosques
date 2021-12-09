package com.example.firealertmonitor_grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MenuPrincipal_Activity extends AppCompatActivity {
    TextView txt_name, txt_email;
    ImageView img_photo;
    HashMap<String, String> info_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        Intent intent = getIntent();
        info_user = (HashMap<String, String>)intent.getSerializableExtra("info_user");

        txt_name = findViewById(R.id.txt_profileName);
        txt_email = findViewById(R.id.txt_email);
        img_photo = findViewById(R.id.img_fotoPerfil);

        txt_name.setText(info_user.get("user_name"));
        txt_email.setText(info_user.get("user_email"));
        String photo = info_user.get("user_photo");
        Picasso.with(getApplicationContext()).load(photo).into(img_photo);
    }

    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, Login_Activity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

}