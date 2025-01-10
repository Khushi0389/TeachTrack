package com.example.teachtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EditStudent extends AppCompatActivity implements View.OnClickListener {

    EditText editTextID,editTextName,editTextCt1,editTextCt2,editTextCt3;
    ImageView imageViewLeft,imageViewRight;
    String name,ID,tableName,credit;
    ArrayList<Integer> studentId = new ArrayList<Integer>();
    int pos; // to track position of the current student being edited.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Edit Marks");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the click listener for the back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        Intent i = getIntent();

        studentId = i.getIntegerArrayListExtra("ID");

        /*name = i.getStringExtra("name");
        ID = i.getStringExtra("ID");*/
        pos = i.getIntExtra("pos",0);
        System.out.println("POS = "+pos);
        ID = studentId.get(pos).toString();
        tableName = i.getStringExtra("table");
        credit = i.getStringExtra("credit");

        final DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        String[] s = databaseHandler.getCtMarks(ID,tableName);

        editTextCt1 = (EditText) findViewById(R.id.editTextCt1);
        editTextCt2 = (EditText) findViewById(R.id.editTextCt2);
        editTextCt3 = (EditText) findViewById(R.id.editTextCt3);
        editTextName = (EditText) findViewById(R.id.editTextStudentNameE);
        editTextID = (EditText) findViewById(R.id.editTextIDE);

        imageViewLeft = (ImageView) findViewById(R.id.imageViewArrowLeft);
        imageViewRight = (ImageView) findViewById(R.id.imageViewArrowRight);

        if(pos==0) //the current position is the first item, so imageviewleft is invisible
            imageViewLeft.setVisibility(View.INVISIBLE);
        if(pos==studentId.size()-1) //the current position is the last item
            imageViewRight.setVisibility(View.INVISIBLE);

        System.out.println("Credit"+credit);

        imageViewRight.setOnClickListener(this);
        imageViewLeft.setOnClickListener(this);

        //set the text of editTextName to the value at index 0 of array s
        editTextName.setText(s[0]);

        //set the text of editTextID to the value of the variable ID.
        editTextID.setText(ID);

        if(s[2]!=null){
            editTextCt1.setText(s[2]);
        }
        if(s[3]!=null){
            editTextCt2.setText(s[3]);
        }
        if(s[4]!=null){
            editTextCt3.setText(s[4]);
        }


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ct1,ct2,ct3,ct4,ct5;
                ct1 = editTextCt1.getText().toString();
                ct2 = editTextCt2.getText().toString();
                ct3 = editTextCt3.getText().toString();
                name = editTextName.getText().toString();
                ID = editTextID.getText().toString();

                databaseHandler.setCtMarks(tableName,ID,name,ct1,ct2,ct3);
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        String ct1,ct2,ct3,ct4,ct5;
        ct1 = editTextCt1.getText().toString();
        ct2 = editTextCt2.getText().toString();
        ct3 = editTextCt3.getText().toString();
        name = editTextName.getText().toString();
        ID = editTextID.getText().toString();

        databaseHandler.setCtMarks(tableName,ID,name,ct1,ct2,ct3);

        if(v.getId()==R.id.imageViewArrowRight){
            pos++;
        }else{
            pos--;
        }

        if(pos==0)
            imageViewLeft.setVisibility(View.INVISIBLE);
        else
            imageViewLeft.setVisibility(View.VISIBLE);

        if(pos==studentId.size()-1)
            imageViewRight.setVisibility(View.INVISIBLE);
        else
            imageViewRight.setVisibility(View.VISIBLE);

        ID = studentId.get(pos).toString();

        String[] s = databaseHandler.getCtMarks(ID,tableName);

        editTextName.setText(s[0]);
        editTextID.setText(ID);
        editTextCt1.setText("");
        if(s[2]!=null){
            editTextCt1.setText(s[2]);
        }
        editTextCt2.setText("");
        if(s[3]!=null){
            editTextCt2.setText(s[3]);
        }
        editTextCt3.setText("");
        if(s[4]!=null){
            editTextCt3.setText(s[4]);
        }

    }
}
