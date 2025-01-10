package com.example.teachtrack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowRecord extends AppCompatActivity {
    int columnSize;
    String[] columnName;
    int[] ID = new int[1000];
    FragmentTransaction mFragmentTransaction;
    FragmentManager mFragmentManager;
    double[][] d;//to store marks
    int rowLength;
    String tableName, credit, path, type;
    ProgressBar progressBar;
    FrameLayout frameLayout;

    View v;//current view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_record);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent i = getIntent();
        tableName = i.getStringExtra("table");
        credit = i.getStringExtra("credit");
        type = i.getStringExtra("type");

        attendanceLoader loader = new attendanceLoader();
        loader.execute(0);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create a new instance of the attendanceLoader class
                attendanceLoader loader = new attendanceLoader();
                // start the asynchronous task to load attendance data.
                loader.execute(1);

                Snackbar.make(view, "CSV created at path TeachTrack/CSV ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();//no action performed so null
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public String createPdf(String csvFilePath, String pdfFilePath, RecyclerView recyclerView) {
        // Read data from CSV file
        List<String[]> rows = readCsv(csvFilePath);
        if (rows == null) {
            return "Failed to read CSV file";
        }

        //PDF FOLDER
        File csvDirectory = new File(Environment.getExternalStorageDirectory(), "TeachTrack/CSV/");
        File pdfDirectory = new File(Environment.getExternalStorageDirectory(), "TeachTrack/PDF/");

        // Create CSV directory if it doesn't exist
        if (!csvDirectory.exists()) {
            if (csvDirectory.mkdirs()) {
                Log.d("Directory Creation", "CSV directory created successfully");
            } else {
                Log.e("Directory Creation", "Failed to create CSV directory");
            }
        }

        // Create PDF directory if it doesn't exist
        if (!pdfDirectory.exists()) {
            if (pdfDirectory.mkdirs()) {
                Log.d("Directory Creation", "PDF directory created successfully");//log debug messages
            } else {
                Log.e("Directory Creation", "Failed to create PDF directory");//log error messages
            }
        }

        // Create PDF document
        PdfDocument pdfDocument = new PdfDocument();
        int pageNumber = 1;
        int itemsPerPage = 20; // Adjust as needed
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();//retrieves the Canvas object associated with the current page of the PDF document
        Paint paint = new Paint();//creates a new instance of the Paint class
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        int startX = 50;
        int startY = 50;

        int columnWidth = 70; // Adjust column width as needed
        int rowHeight = 20;   // Adjust row height as needed

        // Get column headers from the first row of the CSV file
        String[] columnHeaders = rows.get(0);

        // Draw data from CSV onto PDF
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int xPos = startX;

            // Draw horizontal lines between rows
            canvas.drawLine(startX, startY + rowHeight * i, startX + columnWidth * columnHeaders.length, startY + rowHeight * i, paint);

            // Draw text for the cells in the row
            for (int j = 0; j < row.length; j++) {
                if (!isDateColumn(columnHeaders[j])) { // Skip if it's a date column
                    // Draw cell text
                    canvas.drawText(row[j].replace("\"", ""), xPos, startY + rowHeight * (i + 1), paint);

                    // Draw vertical grid lines
                    canvas.drawLine(xPos + columnWidth, startY + rowHeight * i, xPos + columnWidth, startY + rowHeight * (i + 1), paint);
                    xPos += columnWidth;
                }
            }

            // Draw horizontal line between rows
            canvas.drawLine(startX, startY + rowHeight * (i + 1), startX + columnWidth * columnHeaders.length, startY + rowHeight * (i + 1), paint);
        }

        // Draw vertical lines between columns
        int xPos = startX;
        for (int j = 0; j <= columnHeaders.length; j++) {
            canvas.drawLine(xPos, startY, xPos, startY + rowHeight * rows.size(), paint);
            xPos += columnWidth;
        }

        // Finish PDF document
        pdfDocument.finishPage(page);

        // Save PDF file
        File pdfFile = new File(pdfFilePath);
        try {
            FileOutputStream outputStream = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(outputStream);//saving the PDF content to the file
            outputStream.close();//to release system resources and ensure that all data is flushed and written to the file
            pdfDocument.close();
            return "Create PDF if required";
        } catch (IOException e) {
            e.printStackTrace();
            pdfDocument.close();
            return "Failed to export PDF file";
        }
    }


    // Function to check if a column header represents a date column
    private boolean isDateColumn(String columnHeader) {
        // Split the column header into words
        String[] words = columnHeader.split("\\s+");

        // Iterate through each word in the column header
        for (String word : words) {
            // Check if the word contains a combination of lowercase letters and digits
            if (word.matches(".*[a-z].*[0-9].*")) {
                // If such a combination exists in any word, consider it a date column and return true
                return true;
            }
        }

        // If no combination of lowercase letters and digits is found in any word, return false
        return false;
    }


    private List<String[]> readCsv(String csvFilePath) {
        List<String[]> rows = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFilePath));//opens file for reading
            String line;//stores each line read from the CSV file
            while ((line = bufferedReader.readLine()) != null) {
                String[] row = line.split(",");//each line is split into an array of strings
                rows.add(row);
            }
            bufferedReader.close();//After reading all lines
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return rows;
    }

    public boolean createTheoryCSV(String tableName){
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        File file = new File("/sdcard/TeachTrack/CSV/");

        if (!file.exists()) {
            file.mkdirs();
        }

        File csvFile = new File(file, tableName+".csv");

        path = csvFile.getAbsolutePath();
        try {
            if (csvFile.createNewFile()){
                System.out.println("File is created!");
                System.out.println("myfile.csv "+csvFile.getAbsolutePath());
            }else{
                System.out.println("File already exists.");
                System.out.println(file.getAbsolutePath());
            }

            CSVWriter csvWrite = new CSVWriter(new FileWriter(csvFile));
            //get a reference to a readable instance of the SQLite database
            SQLiteDatabase db = databaseHandler.getReadableDatabase();

            Cursor curCSV=db.rawQuery("select * from " + tableName,null);

            //String[] columnNames = curCSV.getColumnNames();
            String[] newColumnNames = new String[10];
            newColumnNames[0] = "ID";
            newColumnNames[1] = "Name";
            newColumnNames[2] = "CT 1";
            newColumnNames[3] = "CT 2";
            newColumnNames[4] = "CT 3";
            newColumnNames[5] = "Total";
            newColumnNames[6] = "Average";

            csvWrite.writeNext(newColumnNames);
            int i = 0;

            //databaseHandler = new DatabaseHandler(this);
           // double[][] d = databaseHandler.getCtMarks(tableName);

            while(curCSV.moveToNext()) {
                System.out.println(curCSV.getString(0));
                String[] arrStr = new String[10];

                arrStr[0] = curCSV.getString(1);
                arrStr[1] = curCSV.getString(0);
                arrStr[2] = curCSV.getString(2);
                arrStr[3] = curCSV.getString(3);
                arrStr[4] = curCSV.getString(4);

                double total = 0;
                int count = 0; // To count the number of CT marks available

// Calculate total and count the number of available CT marks
                for (int k = 1; k <= 3; k++) {//kth CT of ith student (CT-1to3)
                    if (d[k][i] != -1) { // Assuming -1 represents an unavailable CT mark
                        total += d[k][i];
                        count++;
                    }
                }

// Calculate average based on the count of available CT marks
                double avg = (count > 0) ? total / count : 0;

                arrStr[5] = Double.toString(Math.ceil(total));
                arrStr[6] = Double.toString(Math.ceil(avg));


                i++;
                csvWrite.writeNext(arrStr);
            }

            csvWrite.close();
            curCSV.close();
        /*String data="";
        data=readSavedData();
        data= data.replace(",", ";");
        writeData(data);*/

            return true;

        }

        catch(SQLException sqlEx){

            return false;

        }

        catch (IOException e){

            return false;

        }
    }


    public boolean createLabCSV(String tableName){
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        Object[] objects = databaseHandler.getLabMarks(tableName);

        String[][] s = (String[][]) objects[0];

        File file = new File("/sdcard/TeachTrack/CSV/");

        if (!file.exists()) {
            file.mkdirs();
        }

        File csvFile = new File(file, tableName+".csv");

        path = csvFile.getAbsolutePath();
        try {
            if (csvFile.createNewFile()){
                System.out.println("File is created!");
                System.out.println("myfile.csv "+csvFile.getAbsolutePath());
            }else{
                System.out.println("File already exists.");
                System.out.println(file.getAbsolutePath());
            }

            CSVWriter csvWrite = new CSVWriter(new FileWriter(csvFile));

            Object[] object = databaseHandler.getLabMarks(tableName);

            Double[][] marks = (Double[][]) object[1];

            String[] columnNames = {"ID","Name","Lab","Quiz","Viva"};

            csvWrite.writeNext(columnNames);//writes the column names to the CSV file

            for(int i=0;i<rowLength;i++){
                String[] x = new String[10];
                //assigns the value of the second row of data (student IDs)
                // to the first column of the current row in the CSV.
                x[0] = s[1][i];
                //assigns the value of the first row of data (student names)
                // to the second column of the current row in the CSV.
                x[1] = s[0][i];
                double sum=0.0;

                for(int j=0;j<=12;j++)
                    if(marks[j][i]!=-1.0)
                        sum = sum + marks[j][i];

                if(!Double.isNaN(sum))
                    x[2] = Double.toString(sum);//assigns the total sum as a string to the third column of the current row in the CSV.
                else
                    x[2] = "0";
                if(marks[14][i]!=-1)//for quiz
                    x[3] = Double.toString(marks[14][i]);
                else
                    x[3] = "0";
                if(marks[15][i]!=-1)//for viva
                    x[4] = Double.toString(marks[15][i]);
                else
                    x[4] = "0";

                /*if(!Double.isNaN(marks[3][i]))
                    x[5] = Double.toString(Math.ceil(marks[3][i]));
                else
                    x[5] = "0";*/
                csvWrite.writeNext(x);
            }

            csvWrite.close();

        /*String data="";
        data=readSavedData();
        data= data.replace(",", ";");
        writeData(data);*/

            return true;

        }

        catch(SQLException sqlEx){

            return false;

        }

        catch (IOException e){

            return false;

        }
    }

    public boolean createAttendanceCSV(String tableName){
        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        //Object[] objects = databaseHandler.getLabMarks(tableName);

        //String[][] s = (String[][]) objects[0];

        File file = new File("/sdcard/TeachTrack/CSV/");

        if (!file.exists()) {
            file.mkdirs();
        }

        File csvFile = new File(file, tableName+".csv");

        path = csvFile.getAbsolutePath();
        try {
            if (csvFile.createNewFile()){
                System.out.println("File is created!");
                System.out.println("myfile.csv "+csvFile.getAbsolutePath());
            }else{
                System.out.println("File already exists.");
                System.out.println(file.getAbsolutePath());
            }

            CSVWriter csvWrite = new CSVWriter(new FileWriter(csvFile));


            String[] columnNamesP = databaseHandler.columnName(tableName);
            int L = columnNamesP.length;
            String[] columnNames = new String[L+4];

            for(int i=0;i<L;i++)
                columnNames[i] = columnNamesP[i];

            columnNames[L] = "Present";
            columnNames[L+1] = "Absent";
            columnNames[L+2] = "late";
            columnNames[L+3] = "Percentage";
            csvWrite.writeNext(columnNames);

            SQLiteDatabase db = databaseHandler.getReadableDatabase();

            Cursor curCSV=db.rawQuery("select * from " + tableName,null);

            while(curCSV.moveToNext()){
                //iterates over the rows fetched from the SQLite database table
                int p=0,a=0,l=0;
                String[] x = new String[L+4];
                for(int i=0;i<L;i++){
                    x[i] = curCSV.getString(i);
                    if(x[i]==null){
                        a++;
                        x[i] = "A";
                    }
                    else if(x[i].equals("P"))
                        p++;//counts the occurrences
                    else if(x[i].equals("A"))
                        a++;
                    else if(x[i].equals("L"))
                        l++;
                }

                x[L] = Integer.toString(p);
                x[L+1] = Integer.toString(a);
                x[L+2] = Integer.toString(l);
                x[L+3] = Double.toString(Math.ceil(((double)(p+l)/(double)(p+l+a))*100.00))+"%";

                csvWrite.writeNext(x);
            }

            csvWrite.close();

            return true;

        }

        catch(SQLException sqlEx){

            return false;

        }

        catch (IOException e){

            return false;

        }
    }

    //to perform background tasks and to avoid blocking the main UI thread and ensure a smooth user experience.
    public class attendanceLoader extends AsyncTask<Integer,Void,Boolean> {
        private String createPdfMessage;

        @Override
        protected Boolean doInBackground(Integer ... x) {
            if(x[0]==0){
                Bundle bundle = new Bundle();//to pass data between fragments or activities
                bundle.putString("table", tableName);
                bundle.putString("credit",credit);
                bundle.putString("type",type);
                TabFragment fragobj = new TabFragment();//Retrieves the FragmentManager for interacting with fragments associated with this activity
                fragobj.setArguments(bundle);

                mFragmentManager = getSupportFragmentManager();
                mFragmentTransaction = mFragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.containerview,fragobj).commit();

                DatabaseHandler databaseHandler = new DatabaseHandler(ShowRecord.this);

                columnName = databaseHandler.columnName(tableName);
                rowLength = databaseHandler.rowNumber(tableName);
                d = databaseHandler.getCtMarks(tableName);

                for(int i=0;i<=10000;i++)
                    System.out.println(i);

                ///System.out.println("d = "+d[0][0]);

            }else if (x[0] == 1) {
                if (type.equals("Theory")) {
                    createTheoryCSV(tableName);
                    createAttendanceCSV(tableName + "_attendance");

                    createPdfMessage = createPdf("/sdcard/TeachTrack/CSV/" + tableName + ".csv", "/sdcard/TeachTrack/PDF/" + tableName + ".pdf", findViewById(R.id.recyclerView));
                } else {
                    createLabCSV(tableName);
                    createAttendanceCSV(tableName + "_attendance");

                    createPdfMessage = createPdf("/sdcard/TeachTrack/CSV/" + tableName + ".csv", "/sdcard/TeachTrack/PDF/" + tableName + ".pdf", findViewById(R.id.recyclerView));

                }
            }
            return true;
        }

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ShowRecord.this,
                    "Updating",
                    "Updating Data. . .");
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            progressDialog.dismiss();
            if (createPdfMessage != null) {
                Toast.makeText(ShowRecord.this, createPdfMessage, Toast.LENGTH_SHORT).show();
            } else {
               // Toast.makeText(ShowRecord.this, " ", Toast.LENGTH_SHORT).show();
            }
        }


    }

}
