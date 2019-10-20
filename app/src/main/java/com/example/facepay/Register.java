package com.example.facepay;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    private TextView login;
    private Button register_button;
    private EditText editText_email,editText_password;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);





        login = findViewById(R.id.text_view_login);
        register_button = findViewById(R.id.button_register);
        editText_email = findViewById(R.id.text_email);
        editText_password = findViewById(R.id.edit_text_password);
        mAuth = FirebaseAuth.getInstance();
        progressBar =findViewById(R.id.progressBar);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this,Login.class);
                startActivity(intent);
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editText_email.getText().toString().trim();
                String password = editText_password.getText().toString().trim();

                if(email.isEmpty()){
                    editText_email.setError("Email Required");
                    editText_email.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    editText_email.setError("Valid Email Required");
                    editText_email.requestFocus();
                    return;
                }
                if(password.isEmpty() || password.length()<6 ){
                    editText_password.setError("Six Character Password Required");
                    editText_email.requestFocus();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                registerUser(email,password);


            }
        });



    }

    private void registerUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Registration Success
                            Intent intent = new Intent(Register.this,GetExtraInfo.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }else{
                            //Registration Fails
                            Toast.makeText(Register.this, "Registration Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
