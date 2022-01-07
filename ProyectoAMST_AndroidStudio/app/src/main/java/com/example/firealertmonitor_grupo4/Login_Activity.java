package com.example.firealertmonitor_grupo4;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class Login_Activity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText correo, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        correo = findViewById(R.id.txt_email);
        password = findViewById(R.id.txt_password);
        Button btn_login = findViewById(R.id.btn_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");
        if(msg != null){
            if(msg.equals("cerrarSesion")){
                Toast.makeText(getApplicationContext(), "Sesión finalizada", Toast.LENGTH_SHORT).show();
                cerrarSesion();
            }
        }

        btn_login.setOnClickListener(v -> iniciarSesion(mAuth));
    }

    public void registrarUsuario(View view){
        Intent intent = new Intent(this, RegistrarUsuario_Activity.class);
        startActivity(intent);
    }

    public void iniciarSesionGoogle(View view) {
        resultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent()));
    }

    private void cerrarSesion() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
        mAuth.signOut();
    }

    public boolean verificarConexionInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Log.w("TAG", "Falló el inicio de sesión con google", e);
                        Toast.makeText(getApplicationContext(), "Falló el inicio de sesión con google", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        if (verificarConexionInternet()){
            Log.d("TAG", "firebaseAuthWithGoogle: " + acct.getId());
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(getApplicationContext(), "Sesión iniciada como "+acct.getEmail(), Toast.LENGTH_SHORT).show();
                    updateUI(user);
                } else {
                    System.out.println("error");
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    public void iniciarSesion(@NonNull FirebaseAuth mAuth){
        String email = correo.getText().toString();
        String passwd = password.getText().toString();

        if(verificarConexionInternet()){
            if (!(email.equals("") || passwd.equals(""))){
                mAuth.signInWithEmailAndPassword(email, passwd).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getApplicationContext(), "Sesión iniciada como "+email, Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Por favor, ingrese usuario y contraseña",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            HashMap<String, String> info_user = new HashMap<String, String>();
            info_user.put("user_name", user.getDisplayName());
            info_user.put("user_photo", String.valueOf(user.getPhotoUrl()));
            info_user.put("user_id", user.getUid());

            finish();
            Intent intent = new Intent(this, MenuPrincipal_Activity.class);
            intent.putExtra("info_user", info_user);
            startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (verificarConexionInternet()){
            if (currentUser != null) {
                updateUI(currentUser);
                    Toast.makeText(getApplicationContext(), "Sesión iniciada como "+currentUser.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("User is null");
            }
        } else {
            Toast.makeText(getApplicationContext(), "No hay conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }
}