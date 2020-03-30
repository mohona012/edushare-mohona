package com.example.edushare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class DiscussionActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    EditText typeText;
    ScrollView mScrollView;
    String className, userType,currentUserName, currentUserID, currentDate, currentTime, classID;
    FirebaseAuth firebaseAuth;
    TextView displayMessage;
    DatabaseReference userReference, groupNameReference, groupMessageKeyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        typeText=findViewById(R.id.inputmessage);
        mScrollView=findViewById(R.id.chatScrollView);
        displayMessage=findViewById(R.id.userMessages);
        Intent intent=getIntent();
        className=intent.getStringExtra("Class_Name");
        userType=intent.getStringExtra("User_Type");
        classID=intent.getStringExtra("Class_ID");
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(className);
        //changedHere
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserID=firebaseAuth.getCurrentUser().getUid();
        userReference= FirebaseDatabase.getInstance().getReference("users");
        groupNameReference=FirebaseDatabase.getInstance().getReference("discussion").child(className);
        getUserInfo();


    }

    private void getUserInfo() {
        userReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(View view) {
        String message= typeText.getText().toString();
        String messageKey=groupNameReference.push().getKey();
        if(!TextUtils.isEmpty(message)) {
            Calendar calendarDate=Calendar.getInstance();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentDate=simpleDateFormat.format(calendarDate.getTime());

            Calendar calendarTime=Calendar.getInstance();
            SimpleDateFormat simpleTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=simpleTimeFormat.format(calendarTime.getTime());

            HashMap<String,Object> groupmessagekey=new HashMap<>();
            groupNameReference.updateChildren(groupmessagekey);
            groupMessageKeyReference=groupNameReference.child(messageKey);

            HashMap<String,Object> messageInfoMap=new HashMap<>();
            messageInfoMap.put("name", currentUserName );
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate );
            messageInfoMap.put("time", currentTime);
            groupMessageKeyReference.updateChildren(messageInfoMap);
            typeText.setText("");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    DisplayMessage(dataSnapshot);
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    DisplayMessage(dataSnapshot);
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessage(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()) {
            String chatDate= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatUser= (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime= (String) ((DataSnapshot)iterator.next()).getValue();

            displayMessage.append(chatUser+":\n"+chatMessage+ "\n" + chatDate+ "    "+chatTime+"\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
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
            Intent intent=new Intent(DiscussionActivity.this,ClassroomActivity.class);
            intent.putExtra("Class_Name",className);
            intent.putExtra("User_Type", userType);
            intent.putExtra("Class_ID", classID);
            startActivity(intent);
            finish();
        }
        if(item.getItemId()==R.id.homeOption) {
            startActivity(new Intent(DiscussionActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.viewCoursesOption) {
            startActivity(new Intent(DiscussionActivity.this, ClassListActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.addCourseOption) {
            startActivity(new Intent(DiscussionActivity.this, SearchActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(DiscussionActivity.this, SettingsActivity.class));
            finish();
        }
        return true;
    }

}
