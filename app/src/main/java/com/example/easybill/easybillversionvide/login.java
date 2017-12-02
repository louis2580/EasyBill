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
import android.widget.Toast;

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
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userMail;
    private String userPassword;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginout);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        sign_out = (Button) findViewById(R.id.sign_out);

        if (auth.getCurrentUser() != null) {
            // already signed in

            userMail = auth.getCurrentUser().getEmail();
            Toast.makeText(getBaseContext(), " is connected : "+userPassword, Toast.LENGTH_LONG).show();

            sign_out.setEnabled(true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("mail", auth.getCurrentUser().getEmail().toString()); //InputString: from the EditText
            editor.apply();


           sign_out.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   AuthUI.getInstance().signOut(login.this)
                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           Log.d("AUTH", "USER LOG OUT");
                           finish();
                       }
                   });

               }
           });

        } else {

            sign_out.setEnabled(false);
            // not signed in
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            Toast.makeText(getBaseContext(), "on est là", Toast.LENGTH_LONG).show();

            if (resultCode == RESULT_OK){
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                Toast.makeText(getBaseContext(), user.getDisplayName() + " connected as " + user.getEmail(), Toast.LENGTH_LONG).show();
                sign_out.setEnabled(true);

            } else {
                // Sign in failed, check response for error code
                // ...
                Log.d("AUTH", "NOT AUTHENTIFICATED");
            }
        }
    }

}
