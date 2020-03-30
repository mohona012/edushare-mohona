package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ResourcesActivity extends AppCompatActivity {
    String userType;
    String className, classId;
    Uri pdfUri;
    ArrayList<String> resourceArrayList=new ArrayList<>();
    ArrayAdapter<String> resourceAdapter;
    Button uploadButton;
    Toolbar toolbar;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    ListView resourceList;
    TextView selectFileTextView, selectStatusTextView, classNameTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        Intent intent=getIntent();
        userType=intent.getStringExtra("User_Type");
        className=intent.getStringExtra("Class_Name");
        classId=intent.getStringExtra("Class_ID");
        uploadButton=findViewById(R.id.uploader);
        selectFileTextView=findViewById(R.id.selectFileLabel);
        resourceList=findViewById(R.id.resourceListView);
        selectStatusTextView=findViewById(R.id.selectStatusLabel);
        classNameTextView=findViewById(R.id.classNameView);

        classNameTextView.setText(className);
        if(userType.equals("admin")) {
            selectStatusTextView.setVisibility(View.VISIBLE);
            selectFileTextView.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
        }

        toolbar = findViewById(R.id.simplePageToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Class Resources");
        resourceAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, resourceArrayList);
        resourceList.setAdapter(resourceAdapter);

        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference;
        databaseReference=firebaseDatabase.getReference("resources").child(classId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Set<String> set2=new HashSet<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    set.add(""+ds.child("downloadLink").getValue());
                }
                resourceArrayList.clear();
                resourceArrayList.addAll(set);
                resourceAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ResourcesActivity.this, "Error Loading Files", Toast.LENGTH_SHORT).show();
            }
        });

        resourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToClass(resourceArrayList.get(position));
            }
        });

    }

    public void goToClass(String fileName) {

    }

    public void selectResourceToUpload(View view) {
        if(ContextCompat.checkSelfPermission(ResourcesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            selectYourFile();
        } else {
            ActivityCompat.requestPermissions(ResourcesActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==10 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
            selectYourFile();
        } else {
            Toast.makeText(this, "Allow Permission to Continue...", Toast.LENGTH_SHORT).show();
        }

    }

    private void selectYourFile() {
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            selectStatusTextView.setText("File Selected");
        } else {
            Toast.makeText(this, "Select a File", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadFile(View view) {
        if(pdfUri!=null) {
            FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference=firebaseDatabase.getReference("resources");
            final String resourceID = databaseReference.push().getKey();
            final StorageReference storageReference=FirebaseStorage.getInstance().getReference().child("Class Resources").child(classId).child(resourceID);
            storageReference.putFile(pdfUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ResourcesActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                        final String downloadUrl=storageReference.getDownloadUrl().toString();
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("classname", className);
                        hashMap.put("resourceID", resourceID);
                        hashMap.put("dowloadLink", downloadUrl);
                        databaseReference.child(classId).child(resourceID).setValue(hashMap);
                    } else {
                        Toast.makeText(ResourcesActivity.this, "Something is wrong...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Choose File to Upload", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.discussions_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.classroomOption) {
            Intent intent=new Intent(ResourcesActivity.this,ClassroomActivity.class);
            intent.putExtra("Class_Name",className);
            intent.putExtra("User_Type", userType);
            intent.putExtra("Class_ID", classId);
            startActivity(intent);
            finish();
        }
        if(item.getItemId()==R.id.homeOption) {
            startActivity(new Intent(ResourcesActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.viewCoursesOption) {
            startActivity(new Intent(ResourcesActivity.this, ClassListActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCourseOption) {
            startActivity(new Intent(ResourcesActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(ResourcesActivity.this, SettingsActivity.class));
            finish();
        }
        return true;
    }

}
