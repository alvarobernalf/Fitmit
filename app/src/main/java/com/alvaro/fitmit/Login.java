package com.alvaro.fitmit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";

    private EditText emailData, passData;
    private TextView botonOlvidePassword, botonIrRegistro;
    private ProgressBar carga;
    private Button loginButton;
    private boolean loginButtonClicked;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    //Por implementar login button con google
    //private Button loginGoogleButton;

    //Para controlar que el usuario ha pulsado volver 2 veces y cerrar la app
    private boolean doubleBackButtonPressedToExit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButtonClicked = false;
        carga = (ProgressBar) findViewById(R.id.pBarLogin);
        carga.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        loginButton = (Button) findViewById(R.id.procesaLoginButton);
        emailData = (EditText) findViewById(R.id.emailLoginCampo);
        passData = (EditText) findViewById(R.id.passwordLogin);
        botonOlvidePassword = (TextView) findViewById(R.id.forgetPasswordButton);
        botonIrRegistro = (TextView) findViewById(R.id.registrarmeButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Indicamos que el boton ha sido clickado guardando el estado y mostrando la carga.
                loginButtonClicked = true;
                carga.setVisibility(View.VISIBLE);
                //Guardamos los datos del formulario
                final String email = emailData.getText().toString();
                final String password = passData.getText().toString();
                //Validamos que no esten vacios o nulos
                if(validaCampoString(email) || validaCampoString(password)){
                    Toast.makeText(
                            Login.this,
                            "Debes rellenar los campos correctamente",
                            Toast.LENGTH_SHORT).show();
                }else{
                    //Iniciamos proceso de login, comprobando si es completado o no.
                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(Login.this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(
                                                Login.this,
                                                task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }else{
                                        //Comprobamos que se haya verificado
                                        if(auth.getCurrentUser().isEmailVerified()){
                                            Intent i = new Intent(Login.this,PantallaPrincipal.class);
                                            startActivity(i);
                                            finish();
                                            return;
                                        }else{
                                            Toast.makeText(
                                                    Login.this,
                                                    "Por favor verifica tu email",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                    });
                }
                //Paramos la animacion de carga al finalizar
                carga.setVisibility(View.GONE);
            }
        });
        botonOlvidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carga.setVisibility(View.VISIBLE);
                Intent i = new Intent(Login.this,RecuperaContrasena.class);
                startActivity(i);
                finish();
                return;
            }
        });
        //Listener que es invocado cada vez que cambia el estado de usuario
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if(usuario!=null && usuario.isEmailVerified() && !loginButtonClicked){
                    carga.setVisibility(View.VISIBLE);
                    Intent i = new Intent(Login.this,PantallaPrincipal.class);
                    startActivity(i);
                    finish();
                    carga.setVisibility(View.GONE);
                    return;
                }

            }
        };

        botonIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Registro.class);
                startActivity(i);
                finish();
            }
        });


    }
    //Metodo para comprobar que hemos escrito algo en los campos del Login
    private boolean validaCampoString(String str){
        return str.equals("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        if(doubleBackButtonPressedToExit){
            super.onBackPressed();
            finish();
            return;
        }
        this.doubleBackButtonPressedToExit = true;
        Toast.makeText(
                this,
                "Presiona boton volver OTRA VEZ para salir",
                Toast.LENGTH_SHORT).show();

        //Handler para reestablecer el estado de la condicion de cerrar la app, cada 2 segundos
        //se ejecuta
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackButtonPressedToExit=false;
            }
        }, 2000);
    }
}