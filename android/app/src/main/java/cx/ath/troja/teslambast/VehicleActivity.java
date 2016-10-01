package cx.ath.troja.teslambast;

import android.app.Activity;
import android.os.Bundle;

public class VehicleActivity extends Activity {

    public static final String VEHICLE_ID = "cx.ath.troja.teslambast.VehicleActivity.VEHICLE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);
    }
}
