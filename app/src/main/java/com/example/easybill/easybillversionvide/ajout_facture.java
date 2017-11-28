package com.example.easybill.easybillversionvide;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Apero on 18/11/2017.
 */

public class ajout_facture extends AppCompatActivity {
    Spinner spinnerDevise;
    ArrayAdapter<CharSequence> adapterDevise;

    Spinner spinnerDossier;
    ArrayAdapter<CharSequence> adapterDossier;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    // The path to the photo
    private String mCurrentPhotoPath;
    // Get the ImageView
    private ImageView ImageCapture;

    int PHOTO_TAKEN = 1;
    int PHOTO_CANCELLED = -1;
    int PHOTO_WAITING = 2;

    int isPhotoTaken = PHOTO_WAITING;

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

    // Create the File where the photo should go
    File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajout_facture);

        validate = (Button) findViewById(R.id.validate);
        back = (Button) findViewById(R.id.backAddBill);
        Prix = (EditText) findViewById(R.id.prix);
        Lieu = (EditText) findViewById(R.id.lieu);
        Date = (EditText) findViewById(R.id.date);
        //Ajout du sinner devise
        spinnerDevise = (Spinner) findViewById(R.id.devise);
        adapterDevise = ArrayAdapter.createFromResource(this, R.array.devise_names, android.R.layout.simple_spinner_item);
        adapterDevise.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerDevise.setAdapter(adapterDevise);
        spinnerDevise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getItemAtPosition(position)+"is selected", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Ajout du sinner dossier
        spinnerDossier = (Spinner) findViewById(R.id.spinnerDossier);
        adapterDossier = ArrayAdapter.createFromResource(this, R.array.dossier_names, android.R.layout.simple_spinner_item);
        adapterDossier.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerDossier.setAdapter(adapterDossier);
        spinnerDossier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(), parent.getItemAtPosition(position)+"is selected", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


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
                    new DatePickerDialog(ajout_facture.this, date, calendar
                            .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)).show();

                }
            }
        });



        // Ajout du bouton Capture
        Button capture = (Button) findViewById(R.id.addPicture);
        // Get the editText for
        final TextView fichier_image = (TextView) findViewById(R.id.fichier_image);
        // Get the ImageView
        // ImageCapture = (ImageView) findViewById(R.id.ImageCapture);
        /* Listener */
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            dispatchTakePictureIntent(ajout_facture.this);
            //fichier_image.setText(mCurrentPhotoPath.toString());
            }
        });


        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Date.getText().toString().trim().length() == 0 || Lieu.getText().toString().trim().length() == 0 || Prix.getText().toString().trim().length() == 0){
                    Toast.makeText(getBaseContext(), "missing information", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("date", Date.getText().toString()); //InputString: from the EditText
                    editor.putString("place", Lieu.getText().toString()); //InputString: from the EditText
                    editor.putFloat("price", Float.parseFloat(Prix.getText().toString())); //InputString: from the EditText
                    editor.apply();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }



    /**
     * The Android Camera application encodes the photo in the return Intent delivered to
     * onActivityResult() as a small Bitmap in the extras, under the key "data". The following code
     * retrieves this image and displays it in an ImageView.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                ImageCapture.setImageURI(uri);
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageCapture.setImageBitmap(imageBitmap);*/
            }
        }
    }

    /**
     * The {@link } - function that creat a file for the photo.
     */

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * The {@link } - function that invokes an intent to capture a photo.
     */
    public void dispatchTakePictureIntent(Context context) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                galleryAddPic(context);
            }
        }
    }

    /**
     * The following example method demonstrates how to invoke the system's media scanner
     * to add your photo to the Media Provider's database, making it available in the Android Gallery
     * application and to other apps.
     */
    public void galleryAddPic(Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
