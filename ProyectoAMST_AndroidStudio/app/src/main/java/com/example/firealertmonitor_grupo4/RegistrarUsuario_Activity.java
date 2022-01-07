package com.example.firealertmonitor_grupo4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrarUsuario_Activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText correo, password, password_verf;
    private Spinner spinnerTipoUsuario;
    private String tipoUsuario_seleccionado;
    private final String[] arreglo_tipoUsuario = {"User", "Admin"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        correo = findViewById(R.id.txt_emailRegistro);
        password = findViewById(R.id.txt_passwordRegistro);
        password_verf = findViewById(R.id.txt_passwordRegistroVerf);
        Button btn_registrar = findViewById(R.id.btn_registrarUsuario);
        spinnerTipoUsuario = findViewById(R.id.spinner_tipoSensor);

        llenarSpinner();
        btn_registrar.setOnClickListener(v -> registrarUsuario(mAuth));

    }

    private void llenarSpinner(){
        spinnerTipoUsuario.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arreglo_tipoUsuario));

        spinnerTipoUsuario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tipoUsuario_seleccionado = (String) spinnerTipoUsuario.getSelectedItem();
                Toast.makeText(getApplicationContext(), "Tipo de usuario eleccionado: "+tipoUsuario_seleccionado, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No seleccionaron nada
            }
        });
    }

    public boolean verificarConexionInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    private boolean verificarPassword(@NonNull String passwd, String passwVerf){
        if(passwd.equals(passwVerf) && passwd.length()>=6){
            return true;
        } else if (passwd.length()<6){
            Toast.makeText(getApplicationContext(), "La contraseña debe contener mínimo 6 caracteres",Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return false;
        }
    }

    public void registrarUsuario(FirebaseAuth mAuth) {
        String email = correo.getText().toString();
        String passwd = password.getText().toString();
        String passwd_verf = password_verf.getText().toString();

        if (verificarConexionInternet()) {
            if (verificarPassword(passwd, passwd_verf)) {
                mAuth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        /*mDatabase.child("Usuarios").child(userID).child("tipoUsuario").setValue(tipoUsuario_seleccionado);
                        mDatabase.child("Usuarios").child(userID).child("email").setValue(email);
                        mDatabase.child("Usuarios").child(userID).child("contraseña").setValue(passwd);*/

                        // Create a new user with a first and last name
                        Map<String, Object> user = new HashMap<>();
                        user.put("correo", email);
                        user.put("password", passwd);
                        user.put("tipoUsuario", tipoUsuario_seleccionado);

                        db.collection("usuarios").get().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                db.collection("usuarios").document(userID).set(user);
                                Log.d("TAG", "Usuario successfully written!");
                            } else {
                                Log.d("TAG", "Error adding document");
                            }
                        });

                        Log.d("TAG", "createUserWithEmail:success");
                        Toast.makeText(getApplicationContext(), "Usuario " + email + " creado con éxito.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        volver(getCurrentFocus());

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Falló la creación de usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                password.setText("");
                password_verf.setText("");
            }
        } else {
            Toast.makeText(getApplicationContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    public void volver(View view){
        finish();
    }

}