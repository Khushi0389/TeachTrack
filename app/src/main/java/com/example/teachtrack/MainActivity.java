package com.example.teachtrack;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FILE_PERMISSION_CONSTANT = 100;
    private SharedPreferences permissionStatus;
    File localDb; //reference to the database file
    List<Subject> dbList; // to store data retrieved from database
    RelativeLayout relativeLayoutNoCourse; // to display a message when no course are available
    RecyclerView mRecyclerView; // to display the list of subjects
    TextView textView;
    private static final String BACKUP_DIR = "/sdcard/TeachTrack/backup/";

    private static final int    REQUEST_ACCOUNT_PICKER = 1001;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    // Declare databaseHandler as a class-level variable
    private DatabaseHandler databaseHandler;
    private Drive driveService;

    private static final int REQUEST_CODE_FILE_PICKER = 101;
    private static final int REQUEST_CODE_FILE_PICKER_RESTORE = 102;
    Bundle bundle = new Bundle();
    String dbPath;
    // Google Drive variables
    private static final String[] DRIVE_SCOPES = {"https://www.googleapis.com/auth/drive.file"};;
    private GoogleAccountCredential mCredential;
    private static final int RC_SIGN_IN = 123; // Request code for Google Sign-In
    private static final int RC_DRIVE_SIGN_IN = 456; // Request code for Google Drive Sign-In
    private static final int RC_PERMISSION_DRIVE = 789; // Request code for Google Drive Permission

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize databaseHandler
        databaseHandler = new DatabaseHandler(this);

        //This bundle contains data that was saved when the activity was previously destroyed
        // and can be used to restore the activity's state.
        bundle = savedInstanceState;
        overridePendingTransition(R.anim.pull_in_from_left, R.anim.hold);// sets the activity transition animation
        setContentView(R.layout.activity_main);

        // Initialize Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Google Drive credential
        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DRIVE_SCOPES));

        // Check if Google Play Services are available
        if (isGooglePlayServicesAvailable()) {
            // Initialize Google Drive service
            signInWithGoogleDrive();
        } else {
            // Handle case where Google Play Services are not available
            Toast.makeText(this, "Google Play Services are not available", Toast.LENGTH_SHORT).show();
        }

        relativeLayoutNoCourse = findViewById(R.id.realtiveLayoutNoCourse);
        mRecyclerView = findViewById(R.id.recyclerView);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);//used to persist the status of a permission request.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // checks if the Android version running on the device is Android R or later
            if (!Environment.isExternalStorageManager()) { //if the app has been granted the "Manage External Storage" permission.
                showFilePermissionExplanationDialog();
            } else {
                handleFilePermissionGranted();
            }
        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showFilePermissionExplanationDialog();
        } else {
            handleFilePermissionGranted();
        }

        NavigationView navigationView = findViewById(R.id.nav_view);

        Menu menu = navigationView.getMenu();
        MenuItem tools = menu.findItem(R.id.more);
        SpannableString s = new SpannableString(tools.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance44), 0, s.length(), 0);
        tools.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddSubject.class);
                startActivity(intent);
            }
        });
        ///////////////
        TextView textView=findViewById(R.id.bobas);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AboutUsActivity.class);
                startActivity(intent);
            }
        });
        FloatingActionButton ref = findViewById(R.id.ref);
        ref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDatabase();


            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {


            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                // Optional: Add any sliding animation code if needed
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Optional: Add any code when the drawer is opened
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Optional: Add any code when the drawer is closed
            }
        };

        toggle.getDrawerArrowDrawable().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        loadDatabase();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void signInWithGoogleDrive() {
        if (isGooglePlayServicesAvailable()) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                initializeDriveService(account);
            } else {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_DRIVE_SIGN_IN);
            }
        } else {
            // Handle case where Google Play Services are not available
            Toast.makeText(this, "Google Play Services are not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeDriveService(GoogleSignInAccount googleSignInAccount) {
        // Create a Google Drive credential using the provided GoogleSignInAccount
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                MainActivity.this,
                Collections.singleton(DriveScopes.DRIVE_FILE)
        );
        credential.setSelectedAccount(googleSignInAccount.getAccount());

        // Build the Drive service using the credential
        Drive googleDriveService = null;
        try {
            googleDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential
            )
                    .setApplicationName("TeachTrack")
                    .build();
        } catch (Exception e) {
            // Handle any exceptions that occur during initialization
            e.printStackTrace();
            // Log the exception
            Log.e("DriveService", "Error initializing Drive service: " + e.getMessage());
            // Optionally, display a toast or alert to inform the user about the error
            Toast.makeText(MainActivity.this, "Error initializing Drive service", Toast.LENGTH_SHORT).show();
        }

        // Check if the service was successfully initialized
        if (googleDriveService != null) {
            // Now you can use 'googleDriveService' to interact with Google Drive API
            // For example, you can list files, upload files, download files, etc.
            // You may want to store this 'googleDriveService' object as a class variable for later use
        } else {
            // Handle the case where the service failed to initialize
            // You may want to take appropriate action such as retrying initialization or informing the user
        }
    }


    private void handleGoogleDriveSignInResult(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.d("Google Drive", "Google Drive sign-in successful");
                        // Google Drive sign-in is successful, proceed with initializing Drive service
                        initializeDriveService(googleSignInAccount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Google Drive", "Google Drive sign-in failed", e);
                        // Handle sign-in failure here
                        if (e instanceof UserRecoverableAuthException) {
                            // User needs to take action to authorize the app
                            UserRecoverableAuthException recoverableException = (UserRecoverableAuthException) e;
                            // Launch the intent to request authorization from the user
                            startActivityForResult(recoverableException.getIntent(), RC_PERMISSION_DRIVE);
                        } else {
                            // Handle other exceptions
                            Toast.makeText(MainActivity.this, "Google Drive sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            showExitConfirmationDialog();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home) {
            onResume();
        } else if (id == R.id.nav_about_us) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.help) {
            Intent intent = new Intent(MainActivity.this, help.class);
            startActivity(intent);
        }
        else if (id == R.id.backup) {
            backupDatabase();
        }
        else if (id == R.id.restore) {
            if (isBackupAvailable()) {
                showRestoreConfirmationDialog();
            } else {
                Toast.makeText(this, "No backup available", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.action_refresh) {
            loadDatabase(); // Reload the data
        }
        else if (id == R.id.csv) {
            File dir = new File("/sdcard/TeachTrack/csv/");

            // Check if the directory exists and is a directory
            if (dir.exists() && dir.isDirectory()) {
                final String[] listCsv = dir.list();

                // Check if listCsv is not null before accessing its length
                if (listCsv != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    if (listCsv.length != 0) {
                        builder.setTitle("Select to open a file");
                    } else {
                        builder.setTitle("No CSV Exported!");
                    }

                    builder.setItems(listCsv, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            // Do something with the selection
                            String name = listCsv[item];

                            File csvFile = new File("/sdcard/TeachTrack/csv/" + name);

                            // Use FileProvider to get content URI
                            Uri uri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    "com.example.teachtrack.fileprovider",
                                    csvFile
                            );

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "text/csv");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "No Available App to Open CSV!", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    // Handle case where listCsv is null
                    Toast.makeText(getApplicationContext(), "Failed to list CSV files.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Handle case where dir does not exist or is not a directory
                Toast.makeText(getApplicationContext(), "CSV directory does not exist.", Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.pdf) {
            File dir = new File("/sdcard/TeachTrack/csv/");

            // Check if the directory exists and is a directory
            if (dir.exists() && dir.isDirectory()) {
                final String[] listCsv = dir.list();

                // Check if listCsv is not null before accessing its length
                if (listCsv != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    if (listCsv.length != 0) {
                        builder.setTitle("Select CSV to convert to PDF");
                        builder.setItems(listCsv, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                // Do something with the selection
                                String csvFileName = listCsv[item];
                                String csvFilePath = "/sdcard/TeachTrack/csv/" + csvFileName;
                                String pdfFileName = csvFileName.replace(".csv", ".pdf");
                                String pdfFilePath = "/sdcard/TeachTrack/pdf/" + pdfFileName;

                                ShowRecord showRecord = new ShowRecord();
                                showRecord.createPdf(csvFilePath, pdfFilePath, mRecyclerView);

                                // Show an alert with the PDF file location
                                showPdfLocationAlert(pdfFilePath);
                            }
                        });
                    } else {
                        builder.setTitle("No CSV files available");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    // Handle case where listCsv is null
                    Toast.makeText(getApplicationContext(), "Failed to list CSV files.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Handle case where dir does not exist or is not a directory
                Toast.makeText(getApplicationContext(), "CSV directory does not exist.", Toast.LENGTH_LONG).show();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // Method to backup the database file with user's choice
    private void backupDatabase() {
        showBackupOptionDialog();
    }
    // Method to initiate the file picker for backup to Google Drive
    private void backupToGoogleDrive() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            // Perform backup to Google Drive
            new BackupToDriveTask().execute();
        }
    }

    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACCOUNT_PICKER &&
                resultCode == Activity.RESULT_OK && data != null &&
                data.getExtras() != null) {
            String accountName =
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                backupToGoogleDrive(); // Retry backup after account selection
            }
        }
    }

    private class BackupToDriveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                java.io.File backupFile = new java.io.File(dbPath);
                FileContent fileContent = new FileContent("application/octet-stream", backupFile);
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName("backup.db");
                fileMetadata.setMimeType("application/octet-stream");

                Drive.Files.Create createRequest = driveService.files().create(fileMetadata, fileContent);
                createRequest.execute();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Backup to Google Drive completed", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to backup to Google Drive", Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }


    private void restoreFromGoogleDrive() {
        // Fetch the backup file from Google Drive
        new RestoreFromDriveTask().execute();
    }

    private class RestoreFromDriveTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Query files from Google Drive
                FileList result = driveService.files().list()
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .execute();
                List<com.google.api.services.drive.model.File> files = result.getFiles();

                // Search for the backup file
                for (com.google.api.services.drive.model.File file : files) {
                    if (file.getName().equals("backup.db")) {
                        // Download the backup file
                        OutputStream outputStream = new ByteArrayOutputStream();
                        driveService.files().get(file.getId())
                                .executeMediaAndDownloadTo(outputStream);

                        // Restore the database from the downloaded file
                        byte[] byteArray = ((ByteArrayOutputStream) outputStream).toByteArray();
                        FileOutputStream fos = new FileOutputStream(dbPath);
                        fos.write(byteArray);
                        fos.close();

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Database restored from Google Drive", Toast.LENGTH_SHORT).show();
                            loadDatabase(); // Reload the data after restore
                        });
                        return null;
                    }
                }
                // If backup file not found
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Backup file not found on Google Drive", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to restore from Google Drive", Toast.LENGTH_SHORT).show());
            }
            return null;
        }
    }



    // Method to show the backup option dialog (Local or Google Drive)
    private void showBackupOptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Backup Options");
        builder.setMessage("Choose where to backup the database:");

        builder.setPositiveButton("Local Storage", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                backupToLocal();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Google Drive", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                backupToGoogleDrive();

                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void backupToLocal() {
        try {
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            File currentDB = new File(dbPath);//creates a File object for the current database file.
            File backupDB = new File(BACKUP_DIR, "backup.db"); //creates a File object for the backup database file within the backup directory.
// copying database file
            FileChannel src = new FileInputStream(currentDB).getChannel();//creates a channel for the current database file for reading.
            FileChannel dst = new FileOutputStream(backupDB).getChannel();//creates a channel for the backup database file for writing.
            // copies data from the source channel (current database) to the destination channel (backup database).
            dst.transferFrom(src, 0, src.size());
            src.close();//close the input channel
            dst.close();//close the output channel

            Toast.makeText(this, "Backup created successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create backup", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isBackupAvailable() {
        File backupDir = new File(BACKUP_DIR);
        if (backupDir.exists()) {
            File backupDB = new File(backupDir, "backup.db");
            return backupDB.exists();
        }
        return false;
    }
    // Method to restore the database file with user's choice
    private void restoreDatabase() {
        showRestoreOptionDialog();
    }

    // Method to show the backup option dialog (Local or Google Drive)
    private void showRestoreOptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Restore Options");
        builder.setMessage("Choose where to restore from database:");

        builder.setPositiveButton("Local Storage", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restoreFromLocal();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Google Drive", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restoreFromGoogleDrive();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void restoreFromLocal() {
        try {
            File backupDB = new File(BACKUP_DIR, "backup.db");
            File currentDB = new File(dbPath);

            FileChannel src = new FileInputStream(backupDB).getChannel();//read from backup
            FileChannel dst = new FileOutputStream(currentDB).getChannel();//write to current file
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            Toast.makeText(this, "Database restored successfully", Toast.LENGTH_SHORT).show();
            loadDatabase(); // Reload the data after restore
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to restore database", Toast.LENGTH_SHORT).show();
        }
    }
    private void showPdfLocationAlert(String pdfFilePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("PDF Location");
        builder.setMessage("PDF file created at: " + pdfFilePath);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showRestoreConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to restore the database?");

        // Positive button (YES)
        builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked YES
                //  restoreDatabase();
                dialog.dismiss();
            }
        });

        // Negative button (NO)
        builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked NO
                restoreDatabase();
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }
    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Confirmation");
        builder.setMessage("Exit TeachTrack?");

        // Positive button (YES)
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked YES
                finish();
                //  dialog.dismiss();
            }
        });

        // Negative button (NO)
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked NO
                //finish();
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }

    /** Permission Override method**/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FILE_PERMISSION_CONSTANT) { //for the file permissions request.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                handleFilePermissionGranted();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showFilePermissionExplanationDialog();//if the user denies the file permission
                } else {
                    Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void handleFilePermissionGranted() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        localDb = databaseHandler.getDatabasePath();
        dbPath = localDb.getPath();
        dbList = new ArrayList<Subject>();
        dbList = databaseHandler.getDataFromDB();

        if (dbList.isEmpty()) {
            relativeLayoutNoCourse.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            relativeLayoutNoCourse.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mRecyclerView.setHasFixedSize(true);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            RecyclerAdapter mAdapter = new RecyclerAdapter(this, dbList);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mAdapter);

            float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
            RecyclerAdapter.BottomOffsetDecoration bottomOffsetDecoration = new RecyclerAdapter.BottomOffsetDecoration((int) offsetPx);
            mRecyclerView.addItemDecoration(bottomOffsetDecoration);
        }
    }

    private void showFilePermissionExplanationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need File Permission");
            builder.setMessage("This app needs file permission");
            builder.setCancelable(true);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    FILE_PERMISSION_CONSTANT);
        }
// SharedPreferences for storing user preferences, settings, and other small amounts of data that need to be
// persisted even when the app is closed.
        SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(Manifest.permission.READ_EXTERNAL_STORAGE, true);
        editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        editor.apply();
    }
    public void loadDatabase() {
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        dbList = databaseHandler.getDataFromDB();

        if (dbList.isEmpty()) {
            relativeLayoutNoCourse.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            relativeLayoutNoCourse.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mRecyclerView.setHasFixedSize(true);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            RecyclerAdapter mAdapter = new RecyclerAdapter(this, dbList);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setAdapter(mAdapter);

            float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
            RecyclerAdapter.BottomOffsetDecoration bottomOffsetDecoration = new RecyclerAdapter.BottomOffsetDecoration((int) offsetPx);
            mRecyclerView.addItemDecoration(bottomOffsetDecoration);
        }
    }

}
