package com.example.firealertmonitor_grupo4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MenuPrincipal_Activity extends AppCompatActivity {
    TextView txt_name;
    ImageView img_photo;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    HashMap<String, String> info_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        Intent intent = getIntent();
        info_user = (HashMap<String, String>)intent.getSerializableExtra("info_user");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        txt_name = findViewById(R.id.txt_profileName);
        img_photo = findViewById(R.id.img_perfil);
        txt_name.setText("");

        obtenerDatosUsuario();
    }

    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(this, Login_Activity.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

    public void verDispositivosRegistrados(View view){
        Intent intent = new Intent(this, DispositivosRegistrados_Activity.class);
        startActivity(intent);
    }

    public void registrarDispositivo(View view){
        Intent intent = new Intent(this, RegistrarDispositivo_Activity.class);
        startActivity(intent);
    }

    public void obtenerDatosUsuario(){
        String photo = info_user.get("user_photo");
        if (!photo.equals("null")){
            Picasso.with(getApplicationContext()).load(photo).into(img_photo);
        }

        DocumentReference docRef = db.collection("usuarios").document(mAuth.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        txt_name.setText((String) document.get("correo"));
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

}