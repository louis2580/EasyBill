package com.example.easybill.easybillversionvide;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static int ADD_FACTURE = 10;

    private GestureDetectorCompat gestureDetectorCompat;

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;

    ListView billList;
    ArrayList<Bill> bills;
    BillAdapter billAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

        billList = findViewById(R.id.billList);
        Bill exemple = new Bill(0, "Exemple Place", format1.format(Calendar.getInstance().getTime()));

        bills = new ArrayList<Bill>();
        bills.add(exemple);
        billAdapter = new BillAdapter(this, R.layout.liste_facture, bills);
        billList.setAdapter(billAdapter);

        spinner = (Spinner) findViewById(R.id.spinnerDossier);
        adapter = ArrayAdapter.createFromResource(this, R.array.dossier_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getItemAtPosition(position)+"is selected", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Get the button
        FloatingActionButton AjouterFacture =(FloatingActionButton) findViewById(R.id.AjouterFacture);

        AjouterFacture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch another activity
                Intent ajout_facture = new Intent(
                        MainActivity.this, ajout_facture.class);
                startActivityForResult(ajout_facture, ADD_FACTURE);

            }
        });

        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_FACTURE)
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if(resultCode == RESULT_OK) {
                String date_string = prefs.getString("date", "Nothing"); //no id: default value
                String place_string = prefs.getString("place", "Nothing"); //no id: default value
                float price_string = prefs.getFloat("price", 0); //no id: default value

                Bill newBill = new Bill(price_string, place_string, date_string);
                bills.add(newBill);
                billAdapter = new BillAdapter(this, R.layout.liste_facture, bills);
                billList.setAdapter(billAdapter);

            } else
            {

            }

        }
    }

}
