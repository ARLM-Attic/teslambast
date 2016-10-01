package cx.ath.troja.teslambast;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class VehicleActivity extends Activity {

    public static final String VEHICLE_ID = "cx.ath.troja.teslambast.VehicleActivity.VEHICLE_ID";
    protected static Vehicle vehicle = null;

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
            Toast.makeText(this, R.string.invalid_vehicle, Toast.LENGTH_LONG);
            return;
        }

        setTitle(vehicle.name);
    }
}
