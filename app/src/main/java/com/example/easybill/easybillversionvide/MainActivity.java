package com.example.easybill.easybillversionvide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Vector;



public class MainActivity extends AppCompatActivity {


    /* Database */
    DatabaseReference databaseBill;

    static ArrayList<String> FOLDERS = new ArrayList<String>();
    static int ADD_FACTURE = 10;
    static int UPDATE_FACTURE = 20;
    static int RC_SIGN_IN = 30;
    static int RC_SIGN_OUT = 40;

    private GestureDetectorCompat gestureDetectorCompat;
    private String reportFile = "report.txt";

    private FirebaseAuth auth;
    MenuItem logIn;
    MenuItem logOut;
    MenuItem addFolder;
    MenuItem deleteFolder;
    MenuItem createReportFile;

    Spinner spinnerFolder;
    ArrayAdapter<String> adapter;

    ListView billList;
    ArrayList<Bill> bills;
    BillAdapter billAdapter;

    FloatingActionButton AjouterFacture;
    Button newFolder;

    TextView calendar;
    Button getCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar = (TextView) findViewById(R.id.Calendar);
        getCalendar = (Button) findViewById(R.id.getCalendar);

        getCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFromCalendarTable();
            }
        });

        // Get the buttons
        AjouterFacture = (FloatingActionButton) findViewById(R.id.AjouterFacture);
        newFolder = findViewById(R.id.newFolder);

        auth = FirebaseAuth.getInstance();

        spinnerFolder = (Spinner) findViewById(R.id.spinnerDossier);
        billList = findViewById(R.id.billList);
        bills = new ArrayList<Bill>();
        ///////////////////////////////////////////////////////////////////////////
        // When clicking the Add Folder button
        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //switch to another activity
            Intent addFolder = new Intent(MainActivity.this, add_folder.class);
            startActivity(addFolder);
            }
        });

        ////////////////////////////////////////////////////////////////////
        // checks if a user is already logged

        if (auth.getCurrentUser() == null)
        {
            // no one connected
            AjouterFacture.setEnabled(false);
            newFolder.setEnabled(false);

        }
        else
        {
            SyncDatabase(auth.getCurrentUser().getUid());
        }

        ///////////////////////////////////////////////////////////////////////////
        // When clicking the floating Add button
        AjouterFacture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch another activity
                Intent ajout_facture = new Intent(
                        MainActivity.this, ajout_facture.class);
                startActivityForResult(ajout_facture, ADD_FACTURE);
            }
        });


        /////////////////////////////////////////////////
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }



    /////////////////////////////////////////////////////////////////////////////
    // Context menu for long click
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.billList) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(bills.get(info.position).getPlace());
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (auth.getCurrentUser() == null) return true;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.menu);
        // the option selected
        String menuItemName = menuItems[menuItemIndex];
        Bill listItem = bills.get(info.position);

        // action depending on what option was chosen
        switch (menuItemName) {
            case "Modifier":
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("date", listItem.getDate()); //InputString: from the EditText
                editor.putString("place", listItem.getPlace()); //InputString: from the EditText
                editor.putFloat("price", listItem.getPrice()); //InputString: from the EditText
                editor.putString("folder", listItem.getFolder()); //InputString: from the Spinner
                editor.putInt("billID", info.position); //InputString: Bill's id
                editor.apply();
                // switch to edit_bill activity
                Intent edit_bill = new Intent(
                        MainActivity.this, edit_bill.class);
                startActivityForResult(edit_bill, UPDATE_FACTURE);

                break;
            case "Supprimer":
                // Remove the Bill and refresh the View
                DeleteBill(listItem);
                String currentFolder = spinnerFolder.getSelectedItem().toString();
                changeFolder(currentFolder);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void UpdateBillList()
    {

        changeFolder(spinnerFolder.getSelectedItem().toString());
    }

    ////////////////////////////////////////////////////////////////////////////
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //handle 'swipe left' action only

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

         /*
         Toast.makeText(getBaseContext(),
          event1.toString() + "\n\n" +event2.toString(),
          Toast.LENGTH_SHORT).show();
         */

            if (event2.getX() < event1.getX()) {
                Toast.makeText(getBaseContext(),
                        "Swipe left - startActivity()",
                        Toast.LENGTH_SHORT).show();

                //switch another activity
                Intent intent = new Intent(
                        MainActivity.this, second_activity.class);
                startActivity(intent);
            }
            return true;
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    /* Activity result:
        ADD_FACTURE : add a Bill to 'bills' and update the report file
        UPDATE_FACTURE : update a Bill
     */

    /* Create a menu in the upper right corner */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        logIn = menu.findItem (R.id.logIn);
        logOut = menu.findItem(R.id.logOut);
        addFolder = menu.findItem(R.id.addFolder);
        deleteFolder = menu.findItem(R.id.removeFolder);
        createReportFile = menu.findItem(R.id.createReportFile);

        if (auth.getCurrentUser() == null)
        {
            // no one connected
            logIn.setEnabled(true);
            logOut.setEnabled(false);
            deleteFolder.setEnabled(false);
            addFolder.setEnabled(false);
        } else
        {
            logIn.setEnabled(false);
            logOut.setEnabled(true);
            deleteFolder.setEnabled(true);
            addFolder.setEnabled(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_FACTURE) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (resultCode == RESULT_OK) {
                String date_string = prefs.getString("date", "N/C"); //no id: default value
                String place_string = prefs.getString("place", "N/C"); //no id: default value
                float price_string = prefs.getFloat("price", 0); //no id: default value
                String folder_string = prefs.getString("folder", "Autres"); //no id: default value
                String path_string = prefs.getString("path", "N/C"); //no id: default value

                Bill newBill = new Bill(price_string, place_string, date_string, folder_string, path_string);
                // AddBill also sets the ID
                AddBill(newBill);
            }
        }

        if (requestCode == UPDATE_FACTURE) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (resultCode == RESULT_OK) {
                String date_string = prefs.getString("date", "N/C"); //no id: default value
                String place_string = prefs.getString("place", "N/C"); //no id: default value
                float price_float = prefs.getFloat("price", 0); //no id: default value
                String folder_string = prefs.getString("folder", "Autres"); //no id: default value
                int billID = prefs.getInt("billID", -1); //no id: default value

                bills.get(billID).setDate(date_string);
                bills.get(billID).setPlace(place_string);
                bills.get(billID).setPrice(price_float);
                bills.get(billID).setFolder(folder_string);

                UpdateBill(bills.get(billID));

                changeFolder("-");
                spinnerFolder.setSelection(0);
            }
        }
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK){
                // Successfully signed in
                FirebaseUser user = auth.getCurrentUser();

                Toast.makeText(getBaseContext(), user.getDisplayName() + " connected as " + user.getEmail(), Toast.LENGTH_LONG).show();
                logOut.setEnabled(true);
                logIn.setEnabled(false);
                AjouterFacture.setEnabled(true);
                newFolder.setEnabled(true);
                addFolder.setEnabled(true);
                deleteFolder.setEnabled(true);

            } else {
                // Sign in failed, check response for error code
                // ...
                Toast.makeText(getBaseContext(), "La connexion a échouée", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == RC_SIGN_OUT)
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(getBaseContext(), "Vous avez été déconnecté", Toast.LENGTH_LONG).show();
                logIn.setEnabled(true);
                logOut.setEnabled(false);
                AjouterFacture.setEnabled(false);
                newFolder.setEnabled(false);
                addFolder.setEnabled(false);
                deleteFolder.setEnabled(false);

                ////////////////////////////////////////////////////////////////////////
                // Sign out of firebase

                //databaseBill = null;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Create a report when clicking on 'générer compte-rendu'
    private void CreateReport(String filename) throws IOException {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString());
        dir.mkdirs();
        File f = new File(dir, filename);
        f.createNewFile();
        Toast.makeText(getBaseContext(), "Save at : " + f.getPath(), Toast.LENGTH_LONG).show();

        try {

            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            for (Bill billToAdd : bills) {
                String prix = Float.toString(billToAdd.getPrice());
                String chemin;
                if (billToAdd.getPath() != "") {
                    chemin = billToAdd.getPath();
                } else {
                    chemin = "N/C";
                }
                String line = prix
                        + '_' + billToAdd.getPlace()
                        + '_' + billToAdd.getDate()
                        + '_' + billToAdd.getFolder()
                        + "_" + billToAdd.getPath() + "\n";
                // Write the string to the file
                Toast.makeText(getBaseContext(), line, Toast.LENGTH_LONG).show();
                osw.write(line);
            }


                   /* ensure that everything is
                    * really written out and close */

            osw.flush();
            osw.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(getBaseContext(), "error writing file : " + ioe, Toast.LENGTH_LONG).show();

        }

    }

    public ArrayList<Bill> getBillsInFolder(String folder) {
        ArrayList<Bill> billsInFolder = new ArrayList<Bill>();
        for (Bill bill : bills) {
            if (bill.getFolder().equals(folder)) {
                billsInFolder.add(bill);
            }
        }
        return billsInFolder;
    }

    public void changeFolder(String folder)
    {
        if (!folder.equals("-"))
        {
            ArrayList<Bill> folderArray = getBillsInFolder(folder);
            billAdapter = new BillAdapter(this, R.layout.liste_facture, folderArray);
            billList.setAdapter(billAdapter);
        } else {
            billAdapter = new BillAdapter(this, R.layout.liste_facture, bills);
            billList.setAdapter(billAdapter);
        }
    }
    /////////////////////////////////////////////////////
    // When selecting an option in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toast.makeText(getBaseContext(), "ok" +item.getItemId() + " . " + item.toString() , Toast.LENGTH_LONG).show();
        switch(item.getItemId()) {

            case R.id.createReportFile:
                try {
                    CreateReport(reportFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            // Log In
            case R.id.logIn:
                //switch to another activity
                startActivityForResult(
                // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .build(),
                    RC_SIGN_IN);
                return true;

            // Log In
            case R.id.logOut:
                //switch to another activity
                Intent logout = new Intent(MainActivity.this, login.class);
                startActivityForResult(logout, RC_SIGN_OUT);
                return true;


            // Add a folder
            case R.id.addFolder:
                //switch to another activity
                Intent addFolder = new Intent(MainActivity.this, add_folder.class);
                startActivity(addFolder);
                return true;

            // Remove a folder
            case R.id.removeFolder:
                if (!(spinnerFolder.getSelectedItem().toString().equals("-") || spinnerFolder.getSelectedItem().toString().equals("Autres"))) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("Attention !!");
                    alert.setMessage("Supprimer définitivement le dossier " + spinnerFolder.getSelectedItem().toString() +
                            " ? \nCette action est irréversible." +
                            " Vous pouvez conserver les factures de ce dossier. Elles seront déplacées" +
                            " dans le dossier 'Autres'.");

                    alert.setPositiveButton("OUI, avec TOUTES ses factures", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DELETE THE FOLDER AND ALL THE BILLS IN IT
                            showLastChanceDialog(spinnerFolder.getSelectedItem().toString());
                        }
                    });

                    alert.setNegativeButton("OUI, mais conserver les factures", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DELETE THE FOLDER but keep the bills in 'Autres'
                            for (Bill bill : bills) {
                                if (bill.getFolder().equals(spinnerFolder.getSelectedItem().toString())) {
                                    bill.setFolder("Autres");
                                    UpdateBill(bill);
                                }
                            }
                            FOLDERS.remove(spinnerFolder.getSelectedItem().toString());
                            changeFolder("-");
                            spinnerFolder.setSelection(0);
                            dialog.dismiss();
                        }
                    });

                    alert.setNeutralButton("NON, tout conserver", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Doesn't do anything
                            dialog.dismiss();
                        }
                    });

                    alert.show();
                } else {
                    if (spinnerFolder.getSelectedItem().toString().equals("Autres")) {
                        Toast.makeText(getBaseContext(), "Le dossier 'Autres' ne peut pas être supprimé.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Sélectionner un dossier pour effectuer cette action", Toast.LENGTH_LONG).show();
                    }
                }

                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    private void showLastChanceDialog(final String folderToDelete) {
        final AlertDialog.Builder lastAlert = new AlertDialog.Builder(MainActivity.this);
        lastAlert.setTitle("Dernière chance...");
        lastAlert.setMessage("Le dossier " + spinnerFolder.getSelectedItem().toString() +
                " va être supprimer.");

        lastAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DELETE THE FOLDER AND ALL THE BILLS IN IT
                for (int i = 0; i < bills.size(); i++) {
                    if (bills.get(i).getFolder().equals(folderToDelete)) {
                        DeleteBill(bills.get(i));
                        bills.remove(i);
                    }
                }
                FOLDERS.remove(folderToDelete);
                changeFolder("-");
                spinnerFolder.setSelection(0);
                dialog.dismiss();
            }
        });

        lastAlert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DELETE THE FOLDER but keep the bills in 'Autres'

                dialog.dismiss();
            }
        });

        lastAlert.show();
    }

    /////////////////////////////////////////////////////////////////
    // Get all the folders
    public ArrayList<String> getAllFolders() {
        ArrayList<String> allFolders = new ArrayList<String>();
        for(Bill bill : bills)
        {
            // Add the folder if it's not already added
            if (!allFolders.contains(bill.getFolder())) allFolders.add(bill.getFolder());
        }
        return allFolders;
    }

    /* Database*/

    // Add a Bill to the database
    public void AddBill(Bill bill){
        /* Add folder if is not created*/
        // Write a message to the database
        DatabaseReference myRef = databaseBill;
        String Id = myRef.push().getKey();
        bill.setId(Id);
        myRef.child(Id).setValue(bill);
        Toast.makeText(this, "added", Toast.LENGTH_LONG).show();
    }

    // Delete a bill from the database
    public void DeleteBill(Bill bill){
        DatabaseReference myRef = databaseBill.child(bill.getId());
        /*String Id = myRef.getKey();
        myRef.child(Id).removeValue();*/
        myRef.removeValue();
        myRef.child(bill.getId()).setValue(null);
        Toast.makeText(this, "deleted", Toast.LENGTH_LONG).show();
    }

    // Update a Bill in the database
    public void UpdateBill(Bill bill){
        DatabaseReference myRef = databaseBill.child(bill.getId());
        myRef.setValue(bill);
        Toast.makeText(this, "modified", Toast.LENGTH_LONG).show();
    }

    // Get all the Bill in 'bills' ArrayList, used to display them in the ListView
    public void GetAllBills (Map<String,Object> allBills)
    {
        bills.clear();
        if (allBills != null) {
            for (Map.Entry<String, Object> bill : allBills.entrySet()) {
                //Get bills
                Map singleBill = (Map) bill.getValue();

                String date = singleBill.get("date").toString();
                String folder = singleBill.get("folder").toString();
                String id = singleBill.get("id").toString();
                String path = singleBill.get("path").toString();
                String place = singleBill.get("place").toString();
                float price = (float) Float.parseFloat(singleBill.get("price").toString());
                Bill newBill = new Bill(price, place, date, folder, path);
                newBill.setId(id);
                bills.add(newBill);
                if (!FOLDERS.contains(folder))
                {
                    FOLDERS.add(folder);
                }
            }
        }
        UpdateBillList();
    }

    // Get an object Bill from the data got from the database
    public Bill getBillFromDataSnapshot(DataSnapshot dataSnapshot)
    {
        Map singleBill = (Map) dataSnapshot.getValue();

        String place = singleBill.get("place").toString();
        String folder = singleBill.get("folder").toString();
        String date = singleBill.get("date").toString();
        String id = singleBill.get("id").toString();
        String path = singleBill.get("path").toString();
        float price = (float) Float.parseFloat(singleBill.get("price").toString());
        Bill newBill = new Bill(price, place, date, folder, path);
        newBill.setId(id);
        Toast.makeText(this, "Place : " + place + folder + id, Toast.LENGTH_LONG).show();
        return newBill;
    }

    public void DeleteFolder(String folder){
        /* Si existe pas, créée un dossier nommé *folder */
    }


    public void SyncDatabase(String userId)
    {
        FOLDERS.clear();
        // Connexion to Firebase
        Toast.makeText(getBaseContext(), "UID : "+userId, Toast.LENGTH_LONG).show();
        FirebaseDatabase.getInstance().getReference().child(userId).push();
        databaseBill = FirebaseDatabase.getInstance().getReference().child(userId);
        databaseBill.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                bills.add(getBillFromDataSnapshot(dataSnapshot));
                UpdateBillList();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for(Bill bill : bills)
                {
                    if (bill.getId().equals(getBillFromDataSnapshot(dataSnapshot).getId()))
                    {
                        bills.remove(bill);
                        break;
                    }
                }
                bills.add(getBillFromDataSnapshot(dataSnapshot));
                UpdateBillList();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                bills.remove(getBillFromDataSnapshot(dataSnapshot));
                UpdateBillList();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        databaseBill.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get map of users in datasnapshot
                GetAllBills((Map<String, Object>) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ///////////////////////////////////////////////////////////////////////
        // folders spinner:


        // Add the 'all folders' option, all the folders and 'Autres' if it doesn't exist
        FOLDERS.add("-");
        if (!FOLDERS.contains("Autres")) {
            FOLDERS.add("Autres");
        }
        Collections.sort(FOLDERS);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FOLDERS);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        registerForContextMenu(billList);

        spinnerFolder.setAdapter(adapter);
        // When changing the folder to show
        spinnerFolder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getSelectedItem().toString() + " is selected", Toast.LENGTH_LONG).show();
                changeFolder(parent.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
<<<<<<< HEAD

    public void getDataFromCalendarTable() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Toast.makeText(this, "Je passe dans cette méthode de merde ", Toast.LENGTH_LONG).show();
            Cursor cur = null;
            ContentResolver cr = getContentResolver();

            String[] mProjection = new String[] { CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.EVENT_LOCATION };

            /*Uri uri = CalendarContract.Calendars.CONTENT_URI;
            String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
            String[] selectionArgs = new String[]{FirebaseAuth.getInstance().getCurrentUser().getEmail(), "com.google",
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName()};

            Toast.makeText(this, "Before Permission checked ", Toast.LENGTH_LONG).show();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission checked ", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CALENDAR},12);
            }
            Toast.makeText(this, "After Permission checked ", Toast.LENGTH_LONG).show();
            cur = cr.query(uri, mProjection, selection, selectionArgs, null);

            while (cur.moveToNext()) {
                String displayName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
                String accountName = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
                Toast.makeText(this, "Calendar get from "+ accountName, Toast.LENGTH_LONG).show();
                calendar.setText(displayName + " of " + accountName);
            }*/

            Calendar startTime = Calendar.getInstance();
            startTime.set(2017,11,01,00,00);

            Calendar endTime= Calendar.getInstance();
            endTime.set(2017,12,05,00,00);

            // the range is all data from 2014

            String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ))";

            Cursor cursor = this.getBaseContext().getContentResolver().query( CalendarContract.Events.CONTENT_URI, mProjection, selection, null, null );

            // output the events

            if (cursor.moveToFirst()) {
                do {
                    Toast.makeText( this.getApplicationContext(), "Title: " + cursor.getString(1) + " Start-Time: " + (new Date(cursor.getLong(3))).toString(), Toast.LENGTH_LONG ).show();
                    calendar.setText((new Date(cursor.getLong(3))).toString());
                } while ( cursor.moveToNext());
            }




        }
        else{
            Toast.makeText(this, "Not Connected", Toast.LENGTH_LONG).show();
        }
    }
=======
>>>>>>> 0762eab4b98475953575072e4ba463f8e3513571
}
