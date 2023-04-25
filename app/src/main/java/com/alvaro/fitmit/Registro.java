package com.alvaro.fitmit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private Button botonRegistro;
    private ProgressBar carga;
    private EditText emailData, nombreData, passData, numeroData;
    TextView botonIrLogin, enlaceCondiciones;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private String emailRegExp = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]{2,4}"; //Expresion regular validar email
    private static final String TAG = "Registro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        //Campos del formulario y enlace al login
        botonIrLogin = (TextView) findViewById(R.id.volverLoginButton);
        botonRegistro = (Button) findViewById(R.id.procesaRegistroButton);
        emailData = (EditText) findViewById(R.id.emailRegistroCampo);
        nombreData = (EditText) findViewById(R.id.nombreRegistroCampo);
        passData = (EditText) findViewById(R.id.passwordRegistroCampo);
        numeroData = (EditText) findViewById(R.id.numeroRegistroCampo);
        final CheckBox chCondiciones = (CheckBox) findViewById(R.id.chboxRegistro);
        enlaceCondiciones = (TextView) findViewById(R.id.terminosLabelRegistro);
        //Objeto barra de progreso y autenticacion de firebase
        carga = (ProgressBar) findViewById(R.id.pBarRegistro);
        carga.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        //Accion para completar el registro.
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                carga.setVisibility(View.VISIBLE);
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null && user.isEmailVerified()){
                    Intent i = new Intent(Registro.this, PantallaPrincipal.class);
                    startActivity(i);
                    finish();
                    carga.setVisibility(View.GONE);
                    return;
                }
                carga.setVisibility(View.GONE);
            }
        };
        //Accion para volver al login
        botonIrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Registro.this, Login.class);
                startActivity(i);
                finish();
                return;
            }
        });
        //Completar el checkbox y el enlace a los terminos y condiciones.
        chCondiciones.setText("");
        enlaceCondiciones.setText(Html.fromHtml("He leído y acepto "+
                "la <a href = 'https://www.blogger.com/blog/page/edit/preview/2717888006225785268/858014782068900994'>Política de privacidad</a>"));
        enlaceCondiciones.setClickable(true);
        enlaceCondiciones.setMovementMethod(LinkMovementMethod.getInstance());

        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Barra de carga cuando presionamos el boton de registrarse.
                carga.setVisibility(View.VISIBLE);
                //Guardamos lo que el usuario ha escrito
                final String email = emailData.getText().toString();
                final String password = passData.getText().toString();
                final String nombre = nombreData.getText().toString();
                final Boolean isChecked = chCondiciones.isChecked();

                //Validamos lo introducido por el usuario
                if(validaInputs(email,password,nombre,isChecked)){
                    //Una vez validado, se procede a crear el usuario
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Registro.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                //Si no se crea correctamente lo indicamos.
                                Toast.makeText(Registro.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                //Enviamos email de verificacion
                                auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(Registro.this,"Registrado correctamente. "
                                            +" Por favor revisa tu email para verificar tu cuenta. ", Toast.LENGTH_SHORT).show();
                                            String userId = auth.getCurrentUser().getUid();
                                            DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                                            //Creamos al usuario con campo por defecto
                                            Map usuInfo = new HashMap<>();
                                            usuInfo.put("nombre",nombre);
                                            usuInfo.put("imagenPerfilUrl", "default");
                                            currentUserDB.updateChildren(usuInfo);
                                            //Vacio campos y mando al login
                                            emailData.setText("");
                                            nombreData.setText("");
                                            passData.setText("");
                                            numeroData.setText("");
                                            Intent i = new Intent(Registro.this, Login.class);
                                            startActivity(i);
                                            finish();
                                            return;
                                        }else{
                                            Toast.makeText(Registro.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                carga.setVisibility(View.GONE);
            }
        });
    }

    private boolean validaInputs(String email, String password, String nombre, Boolean isChecked){
        if(email.equals("") || nombre.equals("") || password.equals("")){
            Toast.makeText(this,"Todos los campos tienen que estar rellenados", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!email.matches(emailRegExp)){
            Toast.makeText(this,"Escribe un email válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!isChecked){
            Toast.makeText(this,"Por favor, acepta los Términos y condiciones", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        Intent i = new Intent(Registro.this, Login.class);
        startActivity(i);
        finish();

    }
}