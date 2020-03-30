package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ClassroomActivity extends AppCompatActivity {
    TextView teachername, teachermail, classdescription, classschedule;
    FirebaseDatabase firebaseDatabase;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    Toolbar mToolbar;
    DatabaseReference databaseReference;
    Button actionButton;
    String userType;
    String className, classID;
    String mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);
        teachername=findViewById(R.id.classTeacherNameView);
        teachermail=findViewById(R.id.classTeacherMailView);
        actionButton=findViewById(R.id.multipleActionButton);
        classdescription=findViewById(R.id.classDescriptionView);
        classschedule=findViewById(R.id.classTimingView);
        Intent intent=getIntent();
        className=intent.getStringExtra("Class_Name");
        userType=intent.getStringExtra("User_Type");
        classID=intent.getStringExtra("Class_ID");
        mToolbar = findViewById(R.id.simplePageToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(className);


        Toast.makeText(this, userType, Toast.LENGTH_SHORT).show();
        if(!userType.equals("unofficial")) {
            actionButton.setText("Video Lectures");
        }

        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("class").child(classID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String mDes = dataSnapshot.child("classdes").getValue(String.class);
                final String mSchedule=dataSnapshot.child("classdate").getValue(String.class);
                classdescription.setText(mDes);
                classschedule.setText(mSchedule);
                final String teacherID=dataSnapshot.child("uid").getValue(String.class);
                databaseReference=firebaseDatabase.getReference("users").child(teacherID);

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String cTName=dataSnapshot.child("name").getValue(String.class);
                        final String cTMail=dataSnapshot.child("email").getValue(String.class);
                        teachername.setText(cTName);
                        teachermail.setText(cTMail);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void buttonPressAction(View view) {
        if(userType.equals("unofficial")) {
            Intent intent = new Intent(ClassroomActivity.this, JoinProcessActivity.class);
            intent.putExtra("Class_Name", className);
            intent.putExtra("Class_ID", classID);
            startActivity(intent);
            finish();
            return;
        } else { }
    }

    public void viewResourcesAction(View view) {
        if(userType.equals("unofficial")) {
            Toast.makeText(this, "Admit class to view resources", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent=new Intent(ClassroomActivity.this,ResourcesActivity.class);
            intent.putExtra("User_Type",userType);
            intent.putExtra("Class_Name",className);
            intent.putExtra("Class_ID", classID);
            startActivity(intent);
            finish();
            return;
        }
    }

    public void viewDiscussionAction(View view) {
        if(userType.equals("unofficial")) {
            Toast.makeText(this, "Admit class to join discussion", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent=new Intent(ClassroomActivity.this,DiscussionActivity.class);
            intent.putExtra("User_Type",userType);
            intent.putExtra("Class_Name",className);
            intent.putExtra("Class_ID", classID);
            startActivity(intent);
            finish();
            return;
        }
    }

    public void editClassDescription(View view) {
        if(userType.equals("admin")) {
            updateDesorSchedule("des");
        }
    }

    public void editClassSchedule(View view) {
        if(userType.equals("admin")) {
            updateDesorSchedule("schedule");
        }
    }

    private void updateDesorSchedule(final String userfunction) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);

        LinearLayout linearLayout=new LinearLayout(this);
        final EditText uchanges=new EditText(this);
        if(userfunction.equals("des")) {
            builder.setTitle("Edit Description");
            uchanges.setText(classdescription.getText());
        } else {
            builder.setTitle("Edit Schedule");
            uchanges.setText(classschedule.getText());
        }

        uchanges.setMinEms(20);
        linearLayout.addView(uchanges);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userC=uchanges.getText().toString().trim();
                beginEditing(userC, userfunction);
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

    private void beginEditing(final String userchanges, final String eType) {
        if(userchanges.equals("")) {
            Toast.makeText(this, "Write Something...", Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.class_list_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.homeOption) {
            startActivity(new Intent(ClassroomActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCourseOption) {
            startActivity(new Intent(ClassroomActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.createClassOption) {
            startActivity(new Intent(ClassroomActivity.this, CreateClassActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(ClassroomActivity.this, SettingsActivity.class));
            finish();
        }
        return true;
    }
}
