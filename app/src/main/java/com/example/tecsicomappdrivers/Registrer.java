package com.example.tecsicomappdrivers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Registrer extends AppCompatActivity {

    EditText etnombre,etNicname,etPalcaDriver,etRucDriver,etNumberPhone,etDireccion,etEmail,etPassword,etDateBirthay;
    Button btnRegisterDriver;

    // Variables de los Datos que vamos a Registrar.

    String nombre="";
    String nickname="";
    String placaDriver="";
    String rucUser="";
    String numeroTelefono="";
    String direccion="";
    String email="";
    String password="";
    String fecha="2021-09-08";

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // FirebaseAuth
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        etnombre=findViewById(R.id.etNombre);
        etNicname=findViewById(R.id.etNickName);
        etRucDriver=findViewById(R.id.etRucDriver);
        etNumberPhone=findViewById(R.id.etNumberPhone);
        etDireccion=findViewById(R.id.etDireccion);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        etDateBirthay=findViewById(R.id.etDateBirthay);
        btnRegisterDriver=findViewById(R.id.buttonRegistrarDriver);


        btnRegisterDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                nombre=etnombre.getText().toString();
                nickname=etNicname.getText().toString();
                rucUser=etRucDriver.getText().toString();
                numeroTelefono=etNumberPhone.getText().toString();
                direccion=etDireccion.getText().toString();
                email=etEmail.getText().toString();
                password=etPassword.getText().toString();
                etDateBirthay.setText(fecha);
                etDateBirthay.getText().toString();
                ValidarCapos();
            }
        });

    }


    public void ValidarCapos(){
    if (nombre.isEmpty() && nickname.isEmpty() && placaDriver.isEmpty() && rucUser.isEmpty() && numeroTelefono.isEmpty() &&
    direccion.isEmpty() && email.isEmpty() && password.isEmpty() ){
        Toast.makeText(Registrer.this,"Debe Completar los Campos",Toast.LENGTH_SHORT).show();
    }else {
        if (password.length()  >= 6){
            registrarUsers();
        }else {
            Toast.makeText(Registrer.this,"La Contraseña Debe Tener 6 Digitos",Toast.LENGTH_SHORT).show();
        }
    }
    }

    private void registrarUsers(){
    mAuth. createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()){
                Map<String,Object> map = new HashMap<>();
                map.put("NameDriver",nombre);
                map.put("Nickname",nickname);
                map.put("Ruc",rucUser);
                map.put("NumberPhone",numeroTelefono);
                map.put("AddressDriver",direccion);
                map.put("Email",email);
                map.put("Password",password);
                map.put("DateBirthday",fecha);
                String id=mAuth.getCurrentUser().getUid();
                mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task2) {
                        if (task2.isSuccessful()){
                            startActivity(new Intent(Registrer.this,MainActivity.class));
                            finish();
                        }else {
                            Toast.makeText(Registrer.this,"No se pudo Crear los Datos Coorectamente",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }else {
                Toast.makeText(Registrer.this,"No se pudo Registrar Este USer",Toast.LENGTH_SHORT).show();

            }
        }
    });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(Registrer.this,Inicio.class));
            finish();
        }
    }
}