package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class JoinProcessActivity extends AppCompatActivity {
    private Toolbar toolbar;
    FirebaseUser firebaseUser;
    TextView classnameview;
    FirebaseAuth firebaseAuth;
    String className,classID;
    ArrayList<Boolean> classInformation;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_process);
        classnameview=findViewById(R.id.classNameConfirmView);
        toolbar=findViewById(R.id.simplePageToolBar);
        setSupportActionBar(toolbar);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        getSupportActionBar().setTitle("Enroll Now...");
        classInformation=new ArrayList<>();
        Intent intent=getIntent();
        className=intent.getStringExtra("Class_Name");
        classID=intent.getStringExtra("Class_ID");
        classnameview.setText(className);
    }

    public void paidVersionJoinAction(View view) {
        Toast.makeText(this, "Not available now, use trial version", Toast.LENGTH_SHORT).show();
    }

    public void freeTrialJoinAction(View view) {
        addUserToClass();
    }

    private void addUserToClass() {
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("studentlist");
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("classname", className);
        hashMap.put("classid", classID);
        String classlistID = databaseReference.push().getKey();
        databaseReference.child(firebaseUser.getUid()).child(classlistID).setValue(hashMap);
        Toast.makeText(JoinProcessActivity.this, "You have successfully joined the class", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(JoinProcessActivity.this, MainActivity.class));
        finish();
        return;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.join_class_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.homeOption) {
            Intent intent=new Intent(JoinProcessActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        if(item.getItemId()==R.id.viewCoursesOption) {
            startActivity(new Intent(JoinProcessActivity.this, ClassListActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCourseOption) {
            startActivity(new Intent(JoinProcessActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.createClassOption) {
            startActivity(new Intent(JoinProcessActivity.this, CreateClassActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(JoinProcessActivity.this, SettingsActivity.class));
            finish();
        }

        return true;
    }

}
