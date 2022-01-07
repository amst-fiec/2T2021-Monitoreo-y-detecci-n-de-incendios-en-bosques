package com.example.firealertmonitor_grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarDispositivo_Activity extends AppCompatActivity {
    FirebaseFirestore db;
    EditText idSensor, descripcion, latitud, longitud;
    Spinner spinnerSensor;
    String tipo_sensorSeleccionado;
    final String[] arreglo_tipoSensor = {"Temperatura","Humedad","Humo","Fuego"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_dispositivo);

        db = FirebaseFirestore.getInstance();

        idSensor = findViewById(R.id.txt_idSensor);
        descripcion = findViewById(R.id.txt_description);
        latitud = findViewById(R.id.txt_latitud);
        longitud = findViewById(R.id.txt_longitud);
        spinnerSensor = findViewById(R.id.spinner_tipoSensor);

        llenarSpinner();
    }

    public void registrar(View view) {
        if(verificarConexionInternet()){
            if (!(idSensor.getText().toString().isEmpty() || latitud.getText().toString().isEmpty() ||
                    longitud.getText().toString().isEmpty() || descripcion.getText().toString().isEmpty())){

                Map<String, Object> dispositivo = new HashMap<>();
                dispositivo.put("tipoSensor", tipo_sensorSeleccionado);
                dispositivo.put("descripcion", descripcion.getText().toString());
                dispositivo.put("latitud", latitud.getText().toString());
                dispositivo.put("longitud", longitud.getText().toString());

                db.collection("sensores").get().addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        db.collection("sensores").document(idSensor.getText().toString()).set(dispositivo);
                        Log.d("TAG", "Dispositivo successfully written!");
                        Toast.makeText(this, "Dispositivo registrado con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("TAG", "Error adding document");
                        Toast.makeText(this, "Falló el registro del dispositivo", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean verificarConexionInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    private void llenarSpinner(){
        spinnerSensor.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arreglo_tipoSensor));

        spinnerSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tipo_sensorSeleccionado = (String) spinnerSensor.getSelectedItem();
                Toast.makeText(getApplicationContext(), "Tipo de sensor seleccionado: "+tipo_sensorSeleccionado, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No seleccionaron nada
            }
        });
    }

    public void volver(View view){
        finish();
    }
}