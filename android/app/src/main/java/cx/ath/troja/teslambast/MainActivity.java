/*
 * Copyright 2015 The Go Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */

package cx.ath.troja.teslambast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import go.golang.Connection;
import go.golang.Golang;
import go.golang.Vehicles;

import static go.golang.Golang.*;


public class MainActivity extends Activity {

    protected SharedPreferences getPrefs() {
        return getSharedPreferences("teslambast", MODE_PRIVATE);
    }

    protected void log(String m) {
        Log.d("teslambast", m);
    }

    protected void getCredentials(final int msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(cx.ath.troja.teslambast.R.string.credentials);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.credentials_input, null);
        builder.setView(dialogView);

        ((TextView) dialogView.findViewById(R.id.msgLabel)).setText(msg);

        final EditText passwordInput = (EditText) dialogView.findViewById(R.id.password);
        final EditText emailInput = (EditText) dialogView.findViewById(R.id.email);

        builder.setPositiveButton(cx.ath.troja.teslambast.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = ((EditText) dialogView.findViewById(R.id.email)).getText().toString();
                String password = ((EditText) dialogView.findViewById(R.id.password)).getText().toString();
                if (email.equals("") || password.equals("")) {
                    getCredentials(msg);
                } else {
                    SharedPreferences.Editor editor = getPrefs().edit();
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.commit();
                    displayVehicles();
                }
            }
        });

        builder.show();
    }

    protected void displayVehicles() {
        setContentView(cx.ath.troja.teslambast.R.layout.activity_main);

        ListView vehicleList = (ListView) findViewById(cx.ath.troja.teslambast.R.id.vehicles);
        ArrayAdapter vehicleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        vehicleList.setAdapter(vehicleAdapter);

        SharedPreferences prefs = getPrefs();

        Connection conn = Golang.connect(prefs.getString("email", ""), prefs.getString("password", ""));

        if (conn.getHasErr()) {
            if (conn.getErr().contains("401")) {
                getCredentials(R.string.invalid_credentials);
                return;
            }
            throw new RuntimeException(conn.getErr());
        }

        Vehicles vehicles = conn.vehicles();

        if (vehicles.getHasErr()) {
            throw new RuntimeException(vehicles.getErr());
        }

        while (vehicles != null && vehicles.getContent() != null) {
            vehicleAdapter.add(vehicles.getContent().getName());
            vehicles = vehicles.getNext();
        }

        /*
        mTextView = (TextView) findViewById(org.golang.example.bind.R.id.mytextview);

        // Call Go function.
        String greetings = Hello.greetings("Android and Gopher");
        mTextView.setText(greetings);
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getPrefs();
        if (prefs.getString("email", "").equals("") || prefs.getString("password", "").equals("")) {
            getCredentials(R.string.enter_my_tesla_credentials);
        } else {
            displayVehicles();
        }

    }
}
