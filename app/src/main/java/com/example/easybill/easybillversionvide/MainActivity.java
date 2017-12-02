package com.example.easybill.easybillversionvide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;



public class MainActivity extends AppCompatActivity {

    String adresseGmail;
    /* Database */
    DatabaseReference databaseBill;

    static ArrayList<String> FOLDERS = new ArrayList<String>();
    static int ADD_FACTURE = 10;
    static int UPDATE_FACTURE = 20;
    static int MAIL = 30;

    private GestureDetectorCompat gestureDetectorCompat;
    private String reportFile = "report.txt";

    //Drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mToogle;

    Spinner spinnerFolder;
    ArrayAdapter<String> adapter;

    ListView billList;
    ArrayList<Bill> bills;
    BillAdapter billAdapter;

    FloatingActionButton AjouterFacture;
    Button newFolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

        // Drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.menuPanelGauche)));

        mToogle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToogle);
        mToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // button On DrawerPanel
        Button logFacebook = (Button) mDrawerLayout.findViewById(R.id.logFacebook);

        logFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to another activity
                Intent login = new Intent(MainActivity.this, login.class);
                startActivity(login);
            }
        });

        // Get the buttons
        AjouterFacture = (FloatingActionButton) findViewById(R.id.AjouterFacture);
        newFolder = findViewById(R.id.newFolder);

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

        FirebaseDatabase.getInstance().getReference().child("bills").push();
        databaseBill = FirebaseDatabase.getInstance().getReference().child("bills");
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
        FOLDERS.addAll(getAllFolders());
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
        /*
        if (requestCode == MAIL) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (resultCode == RESULT_OK) {
                adresseGmail = prefs.getString("mail", "N/C"); //no id: default value
                if (adresseGmail != null) {
                    Toast.makeText(this, adresseGmail, Toast.LENGTH_LONG).show();
                    Log.d("MAIL", adresseGmail.toString());
                }
            }
        }
        */
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

    //Dynamically create context Menu
    /* @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
        MenuInflater inflater = getMenuInflater();
        if(navPanelEnable)
            inflater.inflate(R.menu.nav_menu, menu);
        else
            inflater.inflate(R.menu.main, menu);
        return super.onPrepareOptionsMenu(menu);
    }*/

    /* Create a menu in the upper right corner */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /////////////////////////////////////////////////////
    // When selecting an option in the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toast.makeText(getBaseContext(), "ok" +item.getItemId() + " . " + item.toString() , Toast.LENGTH_LONG).show();
        if (mToogle.onOptionsItemSelected(item)){
            return true;
        }

        switch(item.getItemId()) {

            case R.id.createReportFile:
                try {
                    CreateReport(reportFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            // Log In
            case R.id.logGoogle:
                //switch to another activity

                Intent login = new Intent(MainActivity.this, login.class);
                startActivityForResult(login, MAIL);
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

    public void SetTheDataBase(){
        databaseBill.setValue("Autres");
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

        FirebaseDatabase databaseDeleteBill = FirebaseDatabase.getInstance();
        DatabaseReference myRef = databaseBill.child(bill.getId());
        /*String Id = myRef.getKey();
        myRef.child(Id).removeValue();*/
        myRef.removeValue();
        myRef.child(bill.getId()).setValue(null);
        Toast.makeText(this, "deleted", Toast.LENGTH_LONG).show();
    }

    // Update a Bill in the database
    public void UpdateBill(Bill bill){
        FirebaseDatabase databaseUpdateBill = FirebaseDatabase.getInstance();
        DatabaseReference myRef = databaseUpdateBill.getReference("bills").child(bill.getId());
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
        return newBill;
    }

    public void DeleteFolder(String folder){
        /* Si existe pas, créée un dossier nommé *folder */
    }

}
