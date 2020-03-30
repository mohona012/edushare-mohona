package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChooseLoginOrRegistrationActivity extends AppCompatActivity {
    private Button login;
    private TextView signup;
    private EditText emailID;
    private TextView passwordRecovery;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_or_registration);
        login=findViewById(R.id.loginButton);
        signup= findViewById(R.id.signupButton);
        emailID=findViewById(R.id.emailReader);
        password=findViewById(R.id.passwordReader);
        passwordRecovery=findViewById(R.id.forgotPasswordLabel);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser!=null) {
                    if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                        Intent intent = new Intent(ChooseLoginOrRegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }
            }
        };
    }

    public void loginAction(View view) {
        final String email=emailID.getText().toString();
        final String passCode=password.getText().toString();
        if(email.equals("")||passCode.equals("")) {
            Toast.makeText(this, "Fill Empty Field", Toast.LENGTH_SHORT).show();
        }else {
            firebaseAuth.signInWithEmailAndPassword(email, passCode).addOnCompleteListener(ChooseLoginOrRegistrationActivity.this,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(ChooseLoginOrRegistrationActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
                            } else if(!firebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                Toast.makeText(ChooseLoginOrRegistrationActivity.this, "Verify Email", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(ChooseLoginOrRegistrationActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }
                    });
        }
    }

    public void signupAction(View view) {
        Intent intent =new Intent(ChooseLoginOrRegistrationActivity.this, RegistrationActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

    public void recoverPassword(View view) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        LinearLayout linearLayout=new LinearLayout(this);
        final EditText uemail=new EditText(this);
        uemail.setHint("Email");
        uemail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        uemail.setMinEms(16);
        linearLayout.addView(uemail);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String umail=uemail.getText().toString().trim();
                beginRecovery(umail);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void beginRecovery(String umail) {
        firebaseAuth.sendPasswordResetEmail(umail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(ChooseLoginOrRegistrationActivity.this, "Recovery Email Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChooseLoginOrRegistrationActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChooseLoginOrRegistrationActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
