package com.example.easybill.easybillversionvide;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.textclassifier.TextClassification;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by louis on 30/11/2017.
 */

public class edit_bill extends AppCompatActivity {

    // Get the button validate
    Button validate;
    // Get the back button
    Button back;
    // Get Prix total
    EditText Prix;
    // Get Lieu
    EditText Lieu;
    // Ajout du calendrier
    Calendar calendar = Calendar.getInstance();
    // Date editText
    EditText Date;
    // Folder
    Spinner Folder;
    ArrayAdapter<CharSequence> adapterDossier;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bill);

        validate = (Button) findViewById(R.id.editValidate);
        back = (Button) findViewById(R.id.backEditBill);
        Prix = (EditText) findViewById(R.id.editPrice);
        Lieu = (EditText) findViewById(R.id.editPlace);
        Date = (EditText) findViewById(R.id.editDate);
        Folder = (Spinner) findViewById(R.id.editSpinnerDossier);

        // Get the default values
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String date_string = prefs.getString("date", "N/C"); //no id: default value
        String place_string = prefs.getString("place", "N/C"); //no id: default value
        float price_float = prefs.getFloat("price", 0); //no id: default value
        String folder_string = prefs.getString("folder", "Autres"); //no id: default value
        final int billID = prefs.getInt("billID", -1); //no id: default value

        // Folder spinner
        adapterDossier = ArrayAdapter.createFromResource(this, R.array.folder_names, android.R.layout.simple_spinner_item);
        int pos = adapterDossier.getPosition(folder_string);
        adapterDossier.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        Folder.setAdapter(adapterDossier);
        Folder.setSelection(pos);

        // Set the default values
        Prix.setText(Float.toString(price_float));
        Lieu.setText(place_string);
        Date.setText(date_string);

        // Création de la fenêtre de dialogue
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Date.setText(new SimpleDateFormat("dd/MM/yy").format(calendar.getTime()));
            }
        };

        // Ouvre le calendrier lorsque Date prend le focus
        Date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    new DatePickerDialog(edit_bill.this, date, calendar
                            .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)).show();

                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Date.getText().toString().trim().length() == 0 || Lieu.getText().toString().trim().length() == 0
                        || Prix.getText().toString().trim().length() == 0 || Folder.getSelectedItem().toString().equals("-")){
                    if (Folder.getSelectedItem().toString().equals("-"))
                    {
                        Toast.makeText(getBaseContext(), "Sélectionner un dossier", Toast.LENGTH_LONG).show();
                    } else
                    {
                        Toast.makeText(getBaseContext(), "Informations manquantes", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                else{
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("date", Date.getText().toString()); //InputString: from the EditText
                    editor.putString("place", Lieu.getText().toString()); //InputString: from the EditText
                    editor.putFloat("price", Float.parseFloat(Prix.getText().toString())); //InputString: from the EditText
                    editor.putString("folder", Folder.getSelectedItem().toString()); //InputString: from the Spinner
                    editor.putInt("billID", billID); //InputString: from the Spinner
                    editor.apply();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

    }
}
