package com.example.easybill.easybillversionvide;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

/**
 * Created by Apero on 30/11/2017.
 */

public class login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    Button sign_out;
    Button back;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userMail;
    private String userPassword;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginout);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        sign_out = (Button) findViewById(R.id.sign_out);
        back = findViewById(R.id.backLogOut);
        TextView mail = findViewById(R.id.userMail);
        TextView name = findViewById(R.id.userName);
        TextView connAs = findViewById(R.id.connectedAs);

        if (auth.getCurrentUser() != null) {
            // already signed in

            userMail = auth.getCurrentUser().getEmail();

            sign_out.setEnabled(true);
            mail.setEnabled(true);
            name.setEnabled(true);
            mail.setText(auth.getCurrentUser().getEmail());
            name.setText(auth.getCurrentUser().getDisplayName());
            connAs.setText("Connecté sous : ");

           sign_out.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   AuthUI.getInstance().signOut(login.this)
                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           setResult(RESULT_OK);
                           finish();
                       }
                   });

               }
           });

        } else {
            connAs.setText("NON Connecté");
            mail.setEnabled(false);
            name.setEnabled(false);
            sign_out.setEnabled(false);
            // not signed in

        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                onBackPressed();
            }
        });
    }
}
