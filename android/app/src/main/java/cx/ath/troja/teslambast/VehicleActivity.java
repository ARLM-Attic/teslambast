package cx.ath.troja.teslambast;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

public class VehicleActivity extends Activity {

    public static final String VEHICLE_ID = "cx.ath.troja.teslambast.VehicleActivity.VEHICLE_ID";
    protected static Vehicle vehicle = null;
    private Handler handler = new Handler();

    @Override
    protected void onPause() {
        ((Button) findViewById(R.id.homelink)).setEnabled(false);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled}
                },
                new int[] { Color.RED }
        );
        ((RadioButton) VehicleActivity.this.findViewById(R.id.connected)).setButtonTintList(colorStateList);
        vehicle.vehicle.close();
        super.onPause();
    }

    public void onResume() {
        super.onResume();

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_enabled}
                },
                new int[] { Color.RED }
        );
        ((RadioButton) VehicleActivity.this.findViewById(R.id.connected)).setButtonTintList(colorStateList);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    vehicle.vehicle.connect();
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ColorStateList colorStateList = new ColorStateList(
                                new int[][]{
                                        new int[]{android.R.attr.state_enabled}
                                },
                                new int[] { Color.GREEN }
                        );
                        ((RadioButton) VehicleActivity.this.findViewById(R.id.connected)).setButtonTintList(colorStateList);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean b;
                                try {
                                    b = vehicle.vehicle.homelinkNearby();
                                } catch (final Exception e) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    return;
                                }
                                if (b) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            final Button button = (Button) VehicleActivity.this.findViewById(R.id.homelink);
                                            button.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                vehicle.vehicle.activateHomelink();
                                                            } catch (final Exception e) {
                                                                handler.post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                                return;
                                                            }
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(VehicleActivity.this, R.string.homelink_activated, Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }
                                            });
                                            button.setEnabled(true);
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        long vehicleID = getIntent().getLongExtra(VEHICLE_ID, 0);
        for (Vehicle v : ((Globals)getApplication()).vehicles) {
            if (v.id == vehicleID) {
                vehicle = v;
            }
        }

        if (vehicle == null) {
            Toast.makeText(this, R.string.invalid_vehicle, Toast.LENGTH_LONG).show();
            return;
        }

        setTitle(vehicle.name);
    }
}
