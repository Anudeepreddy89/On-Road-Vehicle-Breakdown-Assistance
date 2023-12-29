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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.onroadvehicleassistance.utils.NetworkUtils;
import com.onroadvehicleassistance.utils.PDialog;

import java.util.Objects;

public class UserLoginRegistrationActivity extends AppCompatActivity {
    static final String TAG = "UserLoginRegistrationActivity";

    private TextInputLayout fieldEmail;
    private TextInputLayout fieldPassword;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_registration);
        firebaseAuth= FirebaseAuth.getInstance();

        getSupportActionBar().hide();
        initViews();


    }

    private void initViews() {
        fieldEmail = findViewById(R.id.fieldEmail);
        fieldPassword = findViewById(R.id.fieldPassword);

        findViewById(R.id.login).setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                startActivity( new Intent(this, UserHomeActivity.class));
                this.finish();
            }else {
                if (Objects.requireNonNull(fieldEmail.getEditText()).getText().toString().isEmpty() ||
                        Objects.requireNonNull(fieldPassword.getEditText()).getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fields Cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                        String sEmail = fieldEmail.getEditText().getText().toString();
                        String sFieldPassword = fieldPassword.getEditText().getText().toString();
                        if (sEmail.isEmpty() || sFieldPassword.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "All Fields are required", Toast.LENGTH_SHORT).show();
                        } else if (sFieldPassword.length() < 7) {
                            Toast.makeText(getApplicationContext(), "Password Should Greater than 7 Digits", Toast.LENGTH_SHORT).show();
                        } else {
                            PDialog.method(UserLoginRegistrationActivity.this, "Sign in with Email & Password!");
                            PDialog.show();
                            firebaseAuth.signInWithEmailAndPassword(sEmail, sFieldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        checkmailverification();
                                        PDialog.dismiss();
                                    } else {
                                        PDialog.dismiss();
                                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                            Toast.makeText(getApplicationContext(), "Wrong password entered", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Account doesn't exist", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Internet is not available!\nOffline Mode", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
        findViewById(R.id.register).setOnClickListener(view -> {
            if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                String sEmail = fieldEmail.getEditText().getText().toString();
                String sFieldPassword = fieldPassword.getEditText().getText().toString();
                if (sEmail.isEmpty() || sFieldPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All Fields are required", Toast.LENGTH_SHORT).show();
                } else if (sFieldPassword.length() < 7) {
                    Toast.makeText(getApplicationContext(), "Password Should Greater than 7 Digits", Toast.LENGTH_SHORT).show();
                } else {
                    PDialog.method(UserLoginRegistrationActivity.this,"Creating New User!");
                    PDialog.show();
                    firebaseAuth.createUserWithEmailAndPassword(sEmail, sFieldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                                sendEmailVerification();
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    PDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "User already registered", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to Register", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }

            } else {
                Toast.makeText(getApplicationContext(), "Internet is not available!\nOffline Mode", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.forgotPassword).setOnClickListener(view -> {
            startActivity(new Intent(UserLoginRegistrationActivity.this,ForgotActivity.class));
        });
    }

    private void checkmailverification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser.isEmailVerified()==true){
            PDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(UserLoginRegistrationActivity.this,UserHomeActivity.class));
        }
        else{
            PDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Verify your mail first",Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                Toast.makeText(getApplicationContext(),"Verification Email is sent,Verify and Log In Again",Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                PDialog.dismiss();
                /*finish();
                startActivity(new Intent(LoginRegisterActivity.this, HomeActivity.class));*/
            });
        }
        else{
            PDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Failed To Send Verification Email",Toast.LENGTH_SHORT).show();
        }
    }
}