package com.example.teachtrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddStudent extends AppCompatActivity {
    EditText studentID;
    EditText studentName;
    String tableName;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//retrieve the tablename from the intent that launched this activity.
        this.tableName = getIntent().getStringExtra("name");
        this.studentID = (EditText) findViewById(R.id.editTextID);
        this.studentName = (EditText) findViewById(R.id.editTextStudentName);
        ((FloatingActionButton) findViewById(R.id.fabAddStudent)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    String ID = AddStudent.this.studentID.getText().toString();
                    String name = AddStudent.this.studentName.getText().toString();
                    DatabaseHandler db = new DatabaseHandler(AddStudent.this.getApplicationContext());

                    // Check if the ID entered is greater than the last ID in the database
                    if (db.isIDInOrder(AddStudent.this.tableName, ID)) {
                        db.addStudent(name, ID, AddStudent.this.tableName);
                        db.addRoll(AddStudent.this.tableName + "_attendance", Integer.parseInt(ID));
                        Toast.makeText(AddStudent.this.getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        AddStudent.this.finish();
                    } else {
                        // Display an alert message indicating that the entry should be made in a sorted order
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddStudent.this);
                        builder.setMessage("Please enter the student ID in a sorted order.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Do nothing or handle as needed
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
            }
        });

    }
}
