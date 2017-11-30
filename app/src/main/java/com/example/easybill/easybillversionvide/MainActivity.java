package com.example.easybill.easybillversionvide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;

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
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    static int ADD_FACTURE = 10;
    static int UPDATE_FACTURE = 20;

    private GestureDetectorCompat gestureDetectorCompat;

    private String reportFile = "report.txt";

    Spinner spinnerFolder;
    ArrayAdapter<CharSequence> adapter;

    ListView billList;
    ArrayList<Bill> bills;
    BillAdapter billAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

        // Get the button
        FloatingActionButton AjouterFacture =(FloatingActionButton) findViewById(R.id.AjouterFacture);
        Button createReport = findViewById(R.id.createReport);
        spinnerFolder = (Spinner) findViewById(R.id.spinnerDossier);
        billList = findViewById(R.id.billList);
        bills = new ArrayList<Bill>();

        try {
            // Add all the bills in the report to 'bills'
            GetReport(reportFile);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }


        billAdapter = new BillAdapter(this, R.layout.liste_facture, bills);
        billList.setAdapter(billAdapter);

        adapter = ArrayAdapter.createFromResource(this, R.array.folder_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        registerForContextMenu(billList);
        ////////////////////////////////////////////////////////////////////////////
        // Detect long click on the listView


        //////////////////////////////////////////////////////////////////////
        spinnerFolder.setAdapter(adapter);
        // When changing the folder to show
        spinnerFolder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getSelectedItem().toString()+" is selected", Toast.LENGTH_LONG).show();
                if (!parent.getSelectedItem().toString().equals("-")) // ~.getItemAtPosition(position)
                {
                    ArrayList<Bill> folderArray = getBillsInFolder(parent.getItemAtPosition(position).toString());
                    billAdapter = new BillAdapter(parent.getContext(), R.layout.liste_facture, folderArray);
                    billList.setAdapter(billAdapter);
                } else
                {
                    billAdapter = new BillAdapter(parent.getContext(), R.layout.liste_facture, bills);
                    billList.setAdapter(billAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerFolder.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(parent.getContext());
                alert.setTitle("Attention !!");
                alert.setMessage("Supprimer définitivement ce dossier ? \nCette action est irréversible." +
                        " Vous pouvez conserver les factures de ce dossier. Elles seront déplacées" +
                        " dans le dossier 'Autres'.");

                alert.setPositiveButton("OUI, avec TOUTES ses factures", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DELETE THE FOLDER AND ALL THE BILLS IN IT

                        dialog.dismiss();
                    }
                });

                alert.setNeutralButton("OUI, mais conserver les factures", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DELETE THE FOLDER but keep the bills in 'Autres'

                        dialog.dismiss();
                    }
                });

                alert.setNegativeButton("NON, tout conserver", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Doesn't do anything
                        dialog.dismiss();
                    }
                });

                alert.show();

                return true;
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

        ////////////////////////////////////////////////////////////////////////////
        createReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    CreateReport(reportFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }

    /////////////////////////////////////////////////////////////////////////////
    // Context menu for long click
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if (v.getId() == R.id.billList)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(bills.get(info.position).getPlace());
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i < menuItems.length; i++)
            {
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
        switch (menuItemName)
        {
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
                bills.remove(listItem);
                String currentFolder = spinnerFolder.getSelectedItem().toString();
                if (!currentFolder.equals("-")) // ~.getItemAtPosition(position)
                {
                    ArrayList<Bill> folderArray = getBillsInFolder(spinnerFolder.getSelectedItem().toString());
                    billAdapter = new BillAdapter(spinnerFolder.getContext(), R.layout.liste_facture, folderArray);
                    billList.setAdapter(billAdapter);
                } else
                {
                    billAdapter = new BillAdapter(spinnerFolder.getContext(), R.layout.liste_facture, bills);
                    billList.setAdapter(billAdapter);
                }
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

            if(event2.getX() < event1.getX()){
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
        if (requestCode == ADD_FACTURE)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if(resultCode == RESULT_OK) {
                String date_string = prefs.getString("date", "N/C"); //no id: default value
                String place_string = prefs.getString("place", "N/C"); //no id: default value
                float price_string = prefs.getFloat("price", 0); //no id: default value
                String folder_string = prefs.getString("folder", "Autres"); //no id: default value
                String path_string = prefs.getString("path", "N/C"); //no id: default value

                Bill newBill = new Bill(price_string, place_string, date_string, folder_string, path_string);
                bills.add(newBill);
                billAdapter = new BillAdapter(this, R.layout.liste_facture, bills);
                billList.setAdapter(billAdapter);
                try {
                    CreateReport(reportFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == UPDATE_FACTURE)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if(resultCode == RESULT_OK) {
                String date_string = prefs.getString("date", "N/C"); //no id: default value
                String place_string = prefs.getString("place", "N/C"); //no id: default value
                float price_float = prefs.getFloat("price", 0); //no id: default value
                String folder_string = prefs.getString("folder", "Autres"); //no id: default value
                int billID = prefs.getInt("billID", -1); //no id: default value

                bills.get(billID).setDate(date_string);
                bills.get(billID).setPlace(place_string);
                bills.get(billID).setPrice(price_float);
                bills.get(billID).setFolder(folder_string);
                billAdapter = new BillAdapter(this, R.layout.liste_facture, bills);
                billList.setAdapter(billAdapter);
                try {
                    CreateReport(reportFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // Read the report when launching the app
    private void GetReport(String filename) throws IOException
    {
        Vector<String> lines = new Vector<>();
        int billCount = 0;

        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString());
        File f = new File(dir, filename);

        if (f.isFile() && f.canRead()) {
            try {
                //Reading the report file
               /* We have to use the openFileInput()-method
                * the ActivityContext provides.
                * */

                FileInputStream fIn = new FileInputStream(f);
                InputStreamReader isr = new InputStreamReader(fIn);
                BufferedReader br = new BufferedReader(isr);

                String line = "";
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                    billCount++;
                }
                isr.close();
                fIn.close();

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        int billNumber = 0;
        String price = new String();
        float realPrice;
        String place = new String();
        String date = new String();
        String folder = new String();
        String path = new String();


        for (String line : lines)
        {
            String[] parts = line.split("_");
            price = parts[0];
            realPrice = Float.parseFloat(price);
            place = parts[1];
            date = parts[2];
            folder = parts[3];
            path = parts[4];
            bills.add(new Bill(realPrice, place, date, folder, path));
            billNumber++;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Create a report when clicking on 'générer compte-rendu'
    private void CreateReport(String filename) throws IOException
    {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString());
        dir.mkdirs();
        File f = new File(dir, filename);
        f.createNewFile();
        Toast.makeText(getBaseContext(), "Save at : " + f.getPath(), Toast.LENGTH_LONG).show();

        try {

            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            for (Bill billToAdd : bills)
            {
                String prix = Float.toString(billToAdd.getPrice());
                String chemin ;
                if (billToAdd.getPath()!="")
                {
                    chemin = billToAdd.getPath();
                } else
                {
                    chemin = "N/C";
                }
                String line = prix
                        +'_'+ billToAdd.getPlace()
                        +'_'+ billToAdd.getDate()
                        +'_'+ billToAdd.getFolder()
                        +"_"+ billToAdd.getPath()+"\n";
                // Write the string to the file
                Toast.makeText(getBaseContext(), line , Toast.LENGTH_LONG).show();
                osw.write(line);
            }


                   /* ensure that everything is
                    * really written out and close */

            osw.flush();
            osw.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
            Toast.makeText(getBaseContext(), "error writing file : "+ioe, Toast.LENGTH_LONG).show();

        }

    }

    public ArrayList<Bill> getBillsInFolder (String folder)
    {
        ArrayList<Bill> billsInFolder = new ArrayList<Bill>();
        for (Bill bill : bills)
        {
            if (bill.getFolder().equals(folder))
            {
                billsInFolder.add(bill);
            }
        }
        return billsInFolder;
    }

}


// A FAIRE:
/*
        - implémenter la fonction modifier des Bill (cf manifest pour créer nouvelle activité)
        - Implémenter bouton ajouter dossier
 */