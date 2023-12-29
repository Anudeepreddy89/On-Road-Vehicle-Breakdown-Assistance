package com.onroadvehicleassistance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.onroadvehicleassistance.utils.NetworkUtils;
import com.onroadvehicleassistance.utils.PDialog;

import java.util.Objects;

public class AdminLoginActivity extends AppCompatActivity {
    private TextInputLayout fieldEmail;
    private TextInputLayout fieldPassword;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        firebaseAuth= FirebaseAuth.getInstance();

        getSupportActionBar().hide();
        initViews();


    }

    private void initViews() {
        fieldEmail = findViewById(R.id.fieldEmail);
        fieldPassword = findViewById(R.id.fieldPassword);

        findViewById(R.id.login).setOnClickListener(v -> {
                if (Objects.requireNonNull(fieldEmail.getEditText()).getText().toString().isEmpty() ||
                        Objects.requireNonNull(fieldPassword.getEditText()).getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fields Cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                        String sEmail = fieldEmail.getEditText().getText().toString();
                        String sFieldPassword = fieldPassword.getEditText().getText().toString();
                        if (sEmail.equals("admin@mechanic.com") && sFieldPassword.equals("12345678")) {
                            PDialog.method(AdminLoginActivity.this, "Sign in with Email & Password!");
                            PDialog.show();
                            firebaseAuth.signInWithEmailAndPassword(sEmail, sFieldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        PDialog.dismiss();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(),AdminHomeActivity.class));
                                    } else {
                                        PDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Account Doesn't Exist", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Youre not Admin", Toast.LENGTH_SHORT).show();

                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Internet is not available!\nOffline Mode", Toast.LENGTH_SHORT).show();
                    }
                }

        });
    }
    }