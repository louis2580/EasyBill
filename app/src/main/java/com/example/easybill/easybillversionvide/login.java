package com.example.easybill.easybillversionvide;

import android.content.Intent;
import android.os.Bundle;
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
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (auth.getCurrentUser() != null) {
            // already signed in
            setContentView(R.layout.loginout);
            Toast.makeText(getBaseContext(), " is connected", Toast.LENGTH_LONG).show();
            Log.d("AUTH", auth.getCurrentUser().getEmail());

           Button sign_out = (Button) findViewById(R.id.sign_out);
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
            // not signed in
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).setPermissions(Arrays.asList("user_events")).build()))
                            .build(),
                    1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Log.d("AUTH", auth.getCurrentUser().getEmail());
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
                Log.d("AUTH", "NOT AUTHENTIFICATED");
            }
        }
    }

}
