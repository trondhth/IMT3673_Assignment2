package com.example.newsreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PreferencesActivity extends AppCompatActivity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        final EditText txtUrl = findViewById(R.id.txtUrl);
       final EditText txtItems = findViewById(R.id.txtItems);
       final  EditText txtRefresh = findViewById(R.id.txtRfresh);

        final Button btnReturn = findViewById(R.id.btnUpdate);

        btnReturn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String newURL = txtUrl.getText().toString();
                if (txtItems.getText().toString().trim().length() > 0  && txtRefresh.getText().toString().trim().length() > 0) {
                    int items = Integer.parseInt(txtItems.getText().toString());
                    int refreshRate = Integer.parseInt(txtRefresh.getText().toString());
                    final Intent sendBack = new Intent();
                    final Bundle bundle = new Bundle();
                    bundle.putString(MainActivity.NEW_URL, newURL);
                    bundle.putInt(MainActivity.NEW_ITEM_VALUE, items);
                    bundle.putInt(MainActivity.NEW_RRATE, refreshRate);
                    sendBack.putExtras(bundle);
                    setResult(RESULT_OK, sendBack);
                    finish();
                } else {
                    Toast.makeText(getBaseContext(),"Enter valid inputs", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }

}
