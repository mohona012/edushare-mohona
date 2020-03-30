package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ClassListActivity extends AppCompatActivity {
    ListView myClassListView;
    ArrayList<String> myClassNameList =new ArrayList<>();
    ArrayList<String> myClassIDList=new ArrayList<>();
    ArrayAdapter<String> myClassResultAdapter;

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    private Toolbar toolbar;
    String userType="official";
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);
        myClassListView = findViewById(R.id.classListView);
        firebaseDatabase= FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        toolbar=findViewById(R.id.classlistbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Your Classes");
        myClassResultAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, myClassNameList);
        myClassListView.setAdapter(myClassResultAdapter);

        databaseReference=firebaseDatabase.getReference("studentlist").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Set<String> set2=new HashSet<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    set.add(""+ds.child("classname").getValue());
                    set2.add(""+ds.child("classid").getValue());
                }
                myClassNameList.clear();
                myClassNameList.addAll(set);
                myClassIDList.clear();
                myClassIDList.addAll(set2);
                myClassResultAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ClassListActivity.this, "Add Some Courses", Toast.LENGTH_SHORT).show();
            }
        });

        myClassListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToClass(myClassNameList.get(position),myClassIDList.get(position));
            }
        });

    }

    private void goToClass(final String name, final String classid) {
        databaseReference=FirebaseDatabase.getInstance().getReference("class").child(classid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent intent=new Intent(ClassListActivity.this, ClassroomActivity.class);
                intent.putExtra("Class_Name", name);
                intent.putExtra("Class_ID",classid);
                String teacherID=dataSnapshot.child("uid").getValue(String.class);
                if(firebaseUser.getUid().equals(teacherID)) {
                    intent.putExtra("User_Type", "admin");
                } else {
                    intent.putExtra("User_Type", "official");
                }
                startActivity(intent);
                finish();
                return;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
            startActivity(new Intent(ClassListActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCourseOption) {
            startActivity(new Intent(ClassListActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.createClassOption) {
            startActivity(new Intent(ClassListActivity.this, CreateClassActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(ClassListActivity.this, SettingsActivity.class));
            finish();
        }
        return true;
    }

}
