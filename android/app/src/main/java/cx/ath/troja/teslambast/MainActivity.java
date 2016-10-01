package cx.ath.troja.teslambast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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

    private Handler handler = new Handler();

    protected SharedPreferences getPrefs() {
        return getSharedPreferences("teslambast", MODE_PRIVATE);
    }

    public static void log(String m) {
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

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void displayVehicles() {
        setContentView(cx.ath.troja.teslambast.R.layout.activity_main);

        ((Globals) getApplication()).vehicles.clear();

        final ListView vehicleList = (ListView) findViewById(cx.ath.troja.teslambast.R.id.vehicles);
        final ArrayAdapter<Vehicle> vehicleAdapter = new ArrayAdapter<Vehicle>(this, android.R.layout.simple_list_item_1, ((Globals)getApplication()).vehicles) {
            @Override
            public boolean isEnabled(int position) {
                return getItem(position).mobileEnabled;
            }
        };
        vehicleList.setAdapter(vehicleAdapter);
        vehicleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, VehicleActivity.class);
                intent.putExtra(VehicleActivity.VEHICLE_ID, vehicleAdapter.getItem(position).id);
                startActivity(intent);
            }
        });

        final SharedPreferences prefs = getPrefs();

        final ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.show();

        new Thread(new Runnable() {
            public void run() {
                Connection conn;
                try {
                    conn = Golang.connect(prefs.getString("email", ""), prefs.getString("password", ""));
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.dismiss();
                             if (e.getMessage().contains("401")) {
                                 getCredentials(R.string.invalid_credentials);
                             } else {
                                 Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                             }
                        }
                    });
                    return;
                }

                final Vehicles vehicles;
                try {
                    vehicles = conn.vehicles();
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.dismiss();
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                handler.post(new Runnable() {
                    public void run() {
                        progressBar.dismiss();
                        Vehicles next = vehicles;
                        while (next != null && next.getContent() != null) {
                            final Vehicles current = next;
                            final Vehicle vehicle = new Vehicle();
                            vehicle.name = current.getContent().getName();
                            vehicle.id = current.getContent().getID();
                            vehicle.vehicle = current.getContent();
                            vehicleAdapter.add(vehicle);
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        vehicle.mobileEnabled = vehicles.getContent().mobileEnabled();
                                    } catch (final Exception e) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        return;
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (vehicle.mobileEnabled) {
                                                vehicleAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }).start();
                            next = next.getNext();
                        }
                    }
                });
            }
        }).start();
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
