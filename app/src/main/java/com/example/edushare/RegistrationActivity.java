package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {
    private Button register;
    private EditText emailID;
    private EditText userName;
    private RadioGroup gender;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser!=null) {
                    Intent intent=new Intent(RegistrationActivity.this, ChooseLoginOrRegistrationActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };
        register=findViewById(R.id.registerButton);
        emailID=findViewById(R.id.emailReader);
        password=findViewById(R.id.passwordReader);
        userName=findViewById(R.id.nameReader);
        gender=findViewById(R.id.genderIdentification);
    }

    public void registerUser(View view) {
        int selectID=gender.getCheckedRadioButtonId();
        final String email = emailID.getText().toString();
        final String passCode = password.getText().toString();
        final String uName = userName.getText().toString();
        final RadioButton radioButton=findViewById(selectID);

        if (email.equals("") || passCode.equals("") || uName.equals("")) {
            Toast.makeText(this, "Fill Empty Fields", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Incorrect Email Format", Toast.LENGTH_SHORT).show();
        } else if(password.length()<6 ) {
            Toast.makeText(this, "Password should have at least 6 characters", Toast.LENGTH_SHORT).show();
        } else {
            final String uGender = radioButton.getText().toString();
            firebaseAuth.createUserWithEmailAndPassword(email, passCode).addOnCompleteListener(RegistrationActivity.this,
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegistrationActivity.this, "Sign in Successfull, verify your email", Toast.LENGTH_SHORT).show();
                                FirebaseUser user=firebaseAuth.getCurrentUser();
                                String uid=user.getUid();
                                HashMap<Object,String> hashMap=new HashMap<>();
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name",uName);
                                hashMap.put("gender",uGender);
                                hashMap.put("picture", "");
                                hashMap.put("status","");
                                FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference=firebaseDatabase.getReference("users");
                                databaseReference.child(uid).setValue(hashMap);
                                user.sendEmailVerification();
                            }
                        }
                    });
        }
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

    public void loginAction(View view) {
        startActivity(new Intent(RegistrationActivity.this, ChooseLoginOrRegistrationActivity.class));
        finish();
        return;
    }


}
