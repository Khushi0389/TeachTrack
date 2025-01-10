package com.example.teachtrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;
import java.util.Scanner;

public class AddSubject extends AppCompatActivity {

    int count;
    TextView textViewSelectedCSV;
    Spinner spinnerType, spinnerCredit, spinnerSemester;
    Button buttonSelectCSV;
    String filePath;
    Uri fileUri;

    //arrays for populating the snippers.
    String[] Credit = {".75", "1", "1.5", "2", "3", "4"};
    String[] Type = {"Theory", "Lab"};
    String[] Semester = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};

    String deptName, subjectId, subjectName, section;

    boolean flag = false; //to indicate if a CSV file is selected.(initially no)

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.hold, R.anim.pull_out_from_left);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        overridePendingTransition(R.anim.hold, R.anim.pull_in_from_left);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.pull_out_from_left, R.anim.hold);
        setContentView(R.layout.activity_add_subject);
        Toolbar toolbar = findViewById(R.id.toolbarAddSubject);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the click listener for the back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        count = 0;
        final DatabaseHandler db = new DatabaseHandler(this);

        final EditText editTextSubjectName, editTextDeptName, editTextSubjectId, editTextSection;

        editTextDeptName = findViewById(R.id.editTextDeptName);
        editTextSubjectId = findViewById(R.id.editTextSubjectId);
        editTextSubjectName = findViewById(R.id.editTextSubjectName);
        editTextSection = findViewById(R.id.editTextSection);
        textViewSelectedCSV = findViewById(R.id.textViewSelectedCSV);
        buttonSelectCSV = findViewById(R.id.buttonSelectCSV);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCredit = findViewById(R.id.spinnerCredit);
        spinnerSemester = findViewById(R.id.spinnerSemester);

        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, Type);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, Credit);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCredit.setAdapter(adapter);

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, Semester);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSemester.setAdapter(adapter);

        FloatingActionButton fabAddSubject;
        fabAddSubject = findViewById(R.id.fabAddSubject);
        fabAddSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final ProgressDialog progressDialog = new ProgressDialog(AddSubject.this);
                deptName = editTextDeptName.getText().toString();
                subjectId = editTextSubjectId.getText().toString();
                subjectName = editTextSubjectName.getText().toString();
                section = editTextSection.getText().toString();

                // Check if subjectId starts with an alphabet or underscore
                if (!subjectId.isEmpty() && !Character.isLetter(subjectId.charAt(0)) && subjectId.charAt(0) != '_') {
                    Toast.makeText(AddSubject.this, "Subject ID must start with an alphabet or underscore", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (deptName.equals("") || subjectId.equals("") || subjectName.equals("") || section.equals("")) {
                    Snackbar.make(view, "Missing Field!", BaseTransientBottomBar.LENGTH_LONG).show();
                } else if (db.cheeck(subjectId, section) > 0) {
                    Snackbar.make(view, "Change Section!\nYou already have a course with the same section.", BaseTransientBottomBar.LENGTH_LONG).show();
                } else {
                    progressDialog.setTitle("Please wait...");
                    progressDialog.setMessage("Storing Data");
                    progressDialog.show();
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            String type = Type[(int) spinnerType.getSelectedItemId()];
                            String semester = (String) spinnerSemester.getSelectedItem();
                            String credit = (String) spinnerCredit.getSelectedItem();

                            String section = editTextSection.getText().toString();

                            db.addSubject(subjectName, subjectId, deptName, type, section, semester, credit);

                            String s1, s2, table;

                            s1 = subjectId;
                            s2 = section;

                            s1 = s1.replaceAll("\\s", "_");
                            s1 = s1.replaceAll("-", "_");

                            s2 = s2.replaceAll("\\s", "_");
                            s2 = s2.replaceAll("-", "_");

                            table = s1 + "_" + s2;
                            db.createTableAttendance(table + "_attendance");
                            if (type.equals("Theory")) {
                                db.createTableTheory(table);
                            } else {
                                db.createTableLab(table);
                            }
                            if (flag) {
                                InputStream inputStream = null;
                                try {
                                    inputStream = getContentResolver().openInputStream(fileUri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (inputStream != null) {
                                    String text = new Scanner(inputStream).useDelimiter("\\A").next();
                                    text = text.replaceAll("\"", "");
                                    String[] line = text.split("\n");

                                    for (int i = 1; i < line.length; i++) {
                                        String[] word = line[i].split(",");
                                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                                        db.addStudent(word[1], word[0], table);
                                        db.addRoll(table + "_attendance", Integer.parseInt(word[0]));
                                    }
                                }
                            }
                        }
                    });

                    progressDialog.dismiss();

                    finish(); // Finish the AddSubject activity

                    // Start the MainActivity to refresh the data and display the added subject immediately
                    Intent intent = new Intent(AddSubject.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setTitle("Alert");
        localBuilder.setMessage("Do you want to discard the changes?");

        localBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                finish();
            }
        });

        localBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                paramAnonymousDialogInterface.dismiss();
            }
        });
        AlertDialog alert = localBuilder.create();
        alert.show();

        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLUE);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.BLUE);
    }

    public void startFileChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            fileUri = data.getData();

            getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Show alert box informing the user about the CSV requirements
            showCSVRequirementsAlert();

            textViewSelectedCSV.setText(fileUri.toString());
            flag = true;
        }
    }

    private void showCSVRequirementsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CSV Requirements");
        builder.setMessage("The selected CSV must have columns 'ID' and 'Name', and the IDs should be in sorted order.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}