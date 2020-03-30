package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private Toolbar toolbar;
    FirebaseDatabase firebaseDatabase;
    public static final int CAMERA_REQUEST_CODE=100;
    public static final int STORAGE_REQUEST_CODE=200;
    public static final int IMAGE_PICK_GALLERY_CODE=300;
    public static final int IMAGE_PICK_CAMERA_CODE=400;
    String cameraPermissions[];
    String storagePermissions[];
    Uri image_uri;
    DatabaseReference databaseReference;
    StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;
    String storagePath="Users_Profile_Images/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        toolbar=findViewById(R.id.simplePageToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("EduShare");
        loadingBar= new ProgressDialog(this);

        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        databaseReference=firebaseDatabase.getReference("users");
        userProfileImageReference= FirebaseStorage.getInstance().getReference().child("Profile Images");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String mail = ds.child("email").getValue(String.class);
                    if(firebaseUser.getEmail().equals(mail)) {
                        final String mName = ds.child("name").getValue(String.class);
                        final String mEmail=mail;
                        final String mImage=ds.child("picture").getValue(String.class);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public void editProfilePicture(View view) {
        String[] options={"Camera", "Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0) {
                    if(!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }else if(which==1) {
                    if(!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromCamera() {
    }

    private void pickFromGallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_PICK_GALLERY_CODE && resultCode==RESULT_OK && data!=null) {
            loadingBar.setTitle("Set Profile Image");
            loadingBar.setMessage("Please wait while profile image uploads...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            Uri image_uri=data.getData();
            final StorageReference filepath=userProfileImageReference.child(firebaseUser.getUid() +".jpg");
            filepath.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        final String downloadUri=filepath.getDownloadUrl().toString();

                        databaseReference.child(firebaseUser.getUid()).child("picture").setValue(downloadUri)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            loadingBar.dismiss();

                                        } else {
                                            loadingBar.dismiss();
                                            Toast.makeText(MainActivity.this, "Error saving Image", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        loadingBar.dismiss();
                        Toast.makeText(MainActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean checkStoragePermission() {
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result2= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result1 && result2;
    }

    private void requestCameraPermission() {
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case CAMERA_REQUEST_CODE: {
                if(grantResults.length>0) {
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Enable Camera Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;
            case STORAGE_REQUEST_CODE: {
                if(grantResults.length>0) {
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Enable Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //To Change

    public void editUserName(View view) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Edit User Name");
        LinearLayout linearLayout=new LinearLayout(this);
        final EditText uname=new EditText(this);
        uname.setHint("Name");
        uname.setMinEms(16);
        linearLayout.addView(uname);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName=uname.getText().toString().trim();
                beginEditing(userName);
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

    private void beginEditing(String username) {
        if(username=="") {
            Toast.makeText(this, "Write Something...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> results=new HashMap<>();
            results.put("name",username);
            databaseReference.child(firebaseUser.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(MainActivity.this, "Name Updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.viewCoursesOption) {
            startActivity(new Intent(MainActivity.this, ClassListActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCourseOption) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.createClassOption) {
            startActivity(new Intent(MainActivity.this, CreateClassActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.logOutOption) {
            firebaseAuth.getInstance().signOut();
            Intent intent=new Intent(MainActivity.this,ChooseLoginOrRegistrationActivity.class);
            startActivity(intent);
            finish();
        }

        return true;
    }
}
