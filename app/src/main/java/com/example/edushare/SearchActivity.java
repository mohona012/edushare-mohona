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
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    SearchView searchView;
    ListView listView;
    private Toolbar toolbar;
    ArrayList<String> resultList;
    ArrayList<String> resultId;
    ArrayAdapter<String> resultAdapter;

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchView=findViewById(R.id.searchBar);
        listView = findViewById(R.id.resultListView);
        resultId=new ArrayList<>();
        resultList=new ArrayList<>();
        toolbar=findViewById(R.id.simplePageToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Available Classes: ");
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        resultAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,resultList);
        listView.setAdapter(resultAdapter);
        databaseReference=firebaseDatabase.getReference("class");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    resultList.add(""+ds.child("classname").getValue());
                    resultId.add(""+ds.child("classid").getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listView.setAdapter(resultAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listView.setVisibility(View.VISIBLE);
                resultAdapter.getFilter().filter(newText);
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name=parent.getAdapter().getItem(position).toString();
                goToClass(name, resultId.get(resultList.indexOf(name)) );
            }
        });
    }

    public void goToClass(String className, String classID) {
        Toast.makeText(this, className+classID, Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(SearchActivity.this, ClassroomActivity.class);
        intent.putExtra("Class_Name", className);
        intent.putExtra("Class_ID", classID);
        intent.putExtra("User_Type","unofficial");
        startActivity(intent);
        finish();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.homeOption) {
            startActivity(new Intent(SearchActivity.this, MainActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.viewCoursesOption) {
            startActivity(new Intent(SearchActivity.this, ClassListActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.createClassOption) {
            startActivity(new Intent(SearchActivity.this, CreateClassActivity.class));
            finish();
        }
        if(item.getItemId()==R.id.settingsOption) {
            startActivity(new Intent(SearchActivity.this, SettingsActivity.class));
            finish();
        }

        return true;
    }
}
