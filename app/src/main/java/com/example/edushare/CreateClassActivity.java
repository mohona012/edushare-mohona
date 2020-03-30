package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateClassActivity extends AppCompatActivity {
    private EditText readClassName, readClassDescription, readClassSchedule, readClassId;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);
        toolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Your Class");
        readClassId=findViewById(R.id.courseIDReader);
        readClassName=findViewById(R.id.classNameReader);
        readClassDescription=findViewById(R.id.classDescriptionReader);
        readClassSchedule=findViewById(R.id.classScheduleReader);
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference=firebaseDatabase.getReference("class");
    }

    public void registerClass(View view) {
        final String classid=readClassId.getText().toString();
        final String className = readClassName.getText().toString();
        final String classDescription = readClassDescription.getText().toString();
        final String classSchedule=readClassSchedule.getText().toString();

        if (className.equals("") || classDescription.equals("") || classSchedule.equals("")) {
            Toast.makeText(this, "Fill Empty Fields", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseUser user=firebaseAuth.getCurrentUser();
            String uid=user.getUid();
            HashMap<Object,String> hashMap=new HashMap<>();
            hashMap.put("classname",className);
            hashMap.put("classid", classid);
            hashMap.put("uid",uid);
            hashMap.put("classdes",classDescription);
            hashMap.put("classdate",classSchedule);
            hashMap.put("approveStatus", "false");
            databaseReference.child(classid).setValue(hashMap);
            Toast.makeText(this, "Classroom Created", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(CreateClassActivity.this, MainActivity.class));
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.create_class_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.homeOption) {
            startActivity(new Intent(CreateClassActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.viewCoursesOption) {
            startActivity(new Intent(CreateClassActivity.this, ClassListActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCoursesOption) {
            startActivity(new Intent(CreateClassActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(CreateClassActivity.this, SettingsActivity.class));
            finish();
        }
        return true;
    }

}
