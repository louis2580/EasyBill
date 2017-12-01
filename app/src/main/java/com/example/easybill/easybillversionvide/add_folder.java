package com.example.easybill.easybillversionvide;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.easybill.easybillversionvide.MainActivity.FOLDERS;
/**
 * Created by louis on 30/11/2017.
 */

public class add_folder extends AppCompatActivity{


    Button validate;
    Button back;
    EditText folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_folder);
        validate = (Button) findViewById(R.id.validate);
        back = (Button) findViewById(R.id.backAddFolder);
        folderName = (EditText) findViewById(R.id.folderName);

        final ArrayList allFolders = FOLDERS;

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderName.getText().toString().length() == 0)
                {
                    Toast.makeText(getBaseContext(), "Entrer un nom de dossier", Toast.LENGTH_LONG).show();
                    return;
                } else
                {
                    boolean exists = false;
                    for (int i = 0; i<allFolders.size(); i++) {
                        if (folderName.getText().toString().equals(allFolders.get(i)))
                        {
                            exists = true;
                        }
                    }
                    if (exists)
                    {
                        Toast.makeText(getBaseContext(), "Ce dossier existe déjà", Toast.LENGTH_LONG).show();
                        return;
                    } else
                    {
                        FOLDERS.add(folderName.getText().toString());
                        finish();
                    }
                }
            }
        });

    }
}
